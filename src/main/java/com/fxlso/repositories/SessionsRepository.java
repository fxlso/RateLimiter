package com.fxlso.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public class SessionsRepository {
    private final JdbcTemplate jdbcTemplate;

    public SessionsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Creates a new session in the database.
     * @param userId id of the user
     * @param jti unique identifier for the session
     * @param tokenHash hash of the refresh token
     * @param expiresAt expiration time of the session
     */
    public void createSession(String userId, UUID jti, String tokenHash, OffsetDateTime expiresAt) {
        jdbcTemplate.update("""
                INSERT INTO sessions (id, user_id, token_hash, issued_at, expires_at)
                VALUES (?, ?, ?, NOW(), ?)
                """, jti, Long.valueOf(userId), tokenHash, expiresAt);
    }

    /**
     * Checks if a session is active.
     * @param jti unique identifier for the session
     * @param tokenHash hash of the refresh token
     * @return true if the session is active, false otherwise
     */
    public boolean isActive(UUID jti, String tokenHash) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM sessions
                WHERE id = ? AND token_hash = ? AND revoked_at IS NULL AND expires_at > NOW()
                """, Integer.class, jti, tokenHash);
        return count != null && count == 1;
    }

    /**
     * Atomically revokes an active session when its token hash matches.
     * @param jti unique identifier for the session to be revoked
     * @param tokenHash hash of the refresh token associated with the session
     * @param replacementJti unique identifier for the replacement session
     * @return the number of sessions revoked
     */
    public int rotate(UUID jti, String tokenHash, UUID replacementJti) {
        return jdbcTemplate.update("""
                UPDATE sessions
                SET revoked_at = NOW(), replaced_by_jti = ?
                WHERE id = ? AND token_hash = ? AND revoked_at IS NULL AND expires_at > NOW()
                """, replacementJti, jti, tokenHash);
    }

    /**
     * Checks if a session is active by its session ID.
     * @param sessionId unique identifier for the session
     * @return true if the session is active, false otherwise
     */
    public boolean isActiveSession(UUID sessionId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM sessions
                WHERE id = ? AND revoked_at IS NULL AND expires_at > NOW()
                """, Integer.class, sessionId);
        return count != null && count == 1;
    }

    /**
     * Revokes a session and optionally replaces it with another session.
     * @param jti unique identifier for the session to be revoked
     * @param replacementJti  unique identifier for the session that replaces the revoked session (can be null)
     */
    public void revoke(UUID jti, UUID replacementJti) {
        jdbcTemplate.update("UPDATE sessions SET revoked_at = NOW(), replaced_by_jti = ? WHERE id = ? AND revoked_at IS NULL",
                replacementJti, jti);
    }

    /**
     * Revokes a session by its token hash.
     * @param jti unique identifier for the session to be revoked
     * @param tokenHash hash of the refresh token associated with the session
     */
    public void revokeByToken(UUID jti, String tokenHash) {
        jdbcTemplate.update("UPDATE sessions SET revoked_at = NOW() WHERE id = ? AND token_hash = ? AND revoked_at IS NULL",
                jti, tokenHash);
    }

    /**
     * Revokes all sessions for a user except the current session.
     * @param userId the ID of the user
     * @param currentJti the ID of the current session
     */
    public void revokeAllExcept(long userId, UUID currentJti) {
        System.out.println("Revoking all sessions for user " + userId + " except session " + currentJti);

        jdbcTemplate.update("UPDATE sessions SET revoked_at = NOW() WHERE user_id = ? AND id != ? AND revoked_at IS NULL",
                userId, currentJti);
    }
}
