package com.fxlso.repositories;

import com.fxlso.objects.User;
import com.fxlso.util.PasswordUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;

@Repository
public class KeyRepository {

    private final JdbcTemplate jdbcTemplate;

    public KeyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * This method generates a key for the given user and stores it in the database.
     * It will automatically revoke any existing keys for the user before generating a new one.
     * In addition, it will only be one way readable, meaning that once the key is generated, it cannot be retrieved again.
     *
     * @param user The user for whom the key is being generated
     * @param requestLimit The number of requests allowed per time period
     * @param resetIntervalMs The time interval in milliseconds after which the request count is reset
     * @return The generated key as a String
     */
    public String generateKey(User user, int requestLimit, int resetIntervalMs) {

        String newKey = UUID.randomUUID().toString();
        String keyHash = PasswordUtil.hashPassword(newKey);

        // Revoke any existing keys for the user
        jdbcTemplate.update("UPDATE api_keys SET enabled = false WHERE user_id = ? AND enabled = true", Long.valueOf(user.id()));


        // Store the new key in the database
        jdbcTemplate.update("INSERT INTO api_keys (user_id, key_hash, created_at, request_limit, limit_reset_ms) VALUES (?, ?, NOW(), ?, ?)",
                                 Long.valueOf(user.id()), keyHash, requestLimit, resetIntervalMs);

        return newKey;
    }

    /**
     * Generates a key for the given user with default request limit and reset interval
     * Defaults: 1000 requests per minute.
     *
     * See {@link #generateKey(User, int, int)} for more details.
     * @param user
     * @return newly genreated API Key
     */
    public String generateKey(User user) {
        return generateKey(user, 1000, 60 * 60 * 1000); // Default: 1000 requests per minute
    }

    /**
     * This method checks if the provided key is valid and enabled in the database.
     *
     * @param key The key to be validated
     * @return true if the key is valid and enabled, false otherwise
     */
    public boolean isKeyValid(String key) {
        String storedHash = jdbcTemplate.queryForObject(
                "SELECT key_hash FROM api_keys WHERE enabled = true AND key_hash = ?",
                String.class, PasswordUtil.hashPassword(key));
        return storedHash != null;
    }

    /**
     * This method revokes all keys for the given user by setting their enabled status to false in the database.
     *
     * @param user The user whose keys are to be revoked
     * @return true if the keys were successfully revoked, false otherwise
     */
    public boolean revokeKeys(User user) {
        try {
            jdbcTemplate.update("UPDATE api_keys SET enabled = false WHERE user_id = ? AND enabled = true", Long.valueOf(user.id()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns a password-removed User object for the user associated with the given key.
     * @param key The API key to look up the user by
     * @return A User object with the username and ID of the user associated with the key, or null if no such user exists
     */
    public User getUserByKey(String key) {
        String storedHash = PasswordUtil.hashPassword(key);
        return jdbcTemplate.queryForObject(
                "SELECT u.id, u.username FROM users u JOIN api_keys k ON u.id = k.user_id WHERE k.enabled = true AND k.key_hash = ?",
                (rs, rowNum) -> new User(rs.getString("username"), null, String.valueOf(rs.getLong("id"))),
                storedHash);
    }

    /**
     * Checks if the given user has an active API key.
     * @param user The user to check for an active API key
     * @return true if the user has an active API key, false otherwise
     */
    public boolean doesUserHaveActiveKey(User user) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM api_keys WHERE user_id = ? AND enabled = true",
                Integer.class, Long.valueOf(user.id()));
        return count != null && count > 0;
    }

    /**
     * Retrieves the API key details for the given user.
     * @param user The user whose API key details are to be retrieved
     * @return A map containing the user's ID, maximum requests allowed, and limit reset time in milliseconds
     */
    public Map<String, Object> getApiKeyDetailsByUser(User user) {
        return jdbcTemplate.queryForObject(
                "SELECT k.user_id, k.request_limit, k.limit_reset_ms FROM api_keys k WHERE k.user_id = ? AND k.enabled = true",
                (rs, rowNum) -> Map.of(
                        "user_id", rs.getString("user_id"),
                        "request_limit", rs.getInt("request_limit"),
                        "limit_reset_ms", rs.getInt("limit_reset_ms")
                ),
                Long.valueOf(user.id())
        );
    }

}
