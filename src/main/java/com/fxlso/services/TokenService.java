package com.fxlso.services;

import com.fxlso.objects.RefreshToken;
import com.fxlso.objects.TokenPair;
import com.fxlso.repositories.SessionsRepository;
import com.fxlso.repositories.UserRepository;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private final JwtService jwtService;
    private final SessionsRepository sessionsRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshExpirationMs;

    public TokenService(JwtService jwtService, SessionsRepository sessionsRepository, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.sessionsRepository = sessionsRepository;
        this.userRepository = userRepository;
    }

    /**
     * Issues a new access token and refresh token pair for the given username.
     * @param username
     * @return
     */
    public TokenPair issue(String username) {
        RefreshToken refresh = jwtService.generateRefreshToken(username);
        save(username, refresh);
        revokeOthers(username, refresh.jti());
        return new TokenPair(jwtService.generateAccessToken(username, refresh.jti()), refresh.value());
    }

    /**
     * Revokes all other refresh tokens for the user except the current one.
     * @param username
     * @param currentJti
     */
    private void revokeOthers(String username, UUID currentJti) {
        var user = userRepository.getUser(username);
        if (user == null) {
            throw new JwtException("User not found");
        }
        sessionsRepository.revokeAllExcept(Long.parseLong(user.id()), currentJti);
    }

    @Transactional
    public TokenPair refresh(String rawToken) {
        if (!jwtService.isValidRefreshToken(rawToken)) {
            throw new JwtException("Invalid refresh token");
        }

        var jti = jwtService.extractJti(rawToken);
        String username = jwtService.extractUsername(rawToken);
        RefreshToken replacement = jwtService.generateRefreshToken(username);

        log.info("Generating new refresh token for user: {}", username);
        save(username, replacement); // save the new refresh token in the database before rotate, unless will cause foreign key errors

        if (sessionsRepository.rotate(jti, jwtService.hashToken(rawToken), replacement.jti()) != 1) {
            log.warn("Refresh token is revoked or expired: jti={}", jti);
            throw new JwtException("Refresh token is revoked or expired");
        }

        String accessToken = jwtService.generateAccessToken(username, replacement.jti());
        String refreshToken = replacement.value();

        return new TokenPair(accessToken, refreshToken);
    }

    /*
     * Revokes the refresh token in the database.
     */
    public void revoke(String rawToken) {
        if (jwtService.isValidRefreshToken(rawToken)) {
            sessionsRepository.revokeByToken(jwtService.extractJti(rawToken), jwtService.hashToken(rawToken));
        }
    }

    /**
     * Saves the refresh token in the database for the given username.
     * @param username username of the user
     * @param token refresh token to be saved
     */
    private void save(String username, RefreshToken token) {
        log.info("Saving refresh token for user: {}", username);
        var user = userRepository.getUser(username);

        if (user == null) {
            log.error("User not found while attemping to save Token: {}", username);
            throw new JwtException("User not found");
        }

        sessionsRepository.createSession(user.id(), token.jti(), jwtService.hashToken(token.value()),
                OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(refreshExpirationMs / 1_000));
    }
}
