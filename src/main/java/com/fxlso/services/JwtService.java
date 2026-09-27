package com.fxlso.services;

import com.fxlso.objects.RefreshToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.short-lived-token-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshExpirationMs;

    public String generateAccessToken(String username, UUID sessionId) {
        return createToken(username, "access", sessionId, accessExpirationMs);
    }

    public RefreshToken generateRefreshToken(String username) {
        UUID jti = UUID.randomUUID();
        return new RefreshToken(createToken(username, "refresh", jti, refreshExpirationMs), jti);
    }

    private String createToken(String username, String type, UUID jti, long expirationMs) {
        var builder = Jwts.builder()
                .subject(username)
                .claim("type", type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs));
        if (jti != null) {
            builder.id(jti.toString());
            if ("access".equals(type)) {
                builder.claim("sid", jti.toString());
            }
        }
        return builder.signWith(getSignKey()).compact();
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractJti(String token) {
        return UUID.fromString(extractClaim(token, Claims::getId));
    }

    public UUID extractSessionId(String token) {
        return UUID.fromString(extractClaim(token, claims -> claims.get("sid", String.class)));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractClaim(token, claims -> claims.get("type", String.class)));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractClaim(token, claims -> claims.get("type", String.class)));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return isAccessToken(token)
                && extractUsername(token).equals(userDetails.getUsername())
                && !extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean isValidRefreshToken(String token) {
        try {
            return isRefreshToken(token)
                    && !extractClaim(token, Claims::getExpiration).before(new Date()) // Check if expiration claim is not before the current date
                    && extractJti(token) != null;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String hashToken(String token) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(getSignKey());
            return HexFormat.of().formatHex(mac.doFinal(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash token", e);
        }
    }

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> resolver) {
        return resolver.apply(Jwts.parser().verifyWith(getSignKey()).build()
                .parseSignedClaims(token).getPayload());
    }
}
