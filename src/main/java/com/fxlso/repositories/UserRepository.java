package com.fxlso.repositories;

import com.fxlso.objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepository {

    private final Map<String, User> userCache = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getString("username"),
            rs.getString("password"),
            String.valueOf(rs.getLong("id"))
    );
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Registers a new user in the database.
     * @param username 255 characters
     * @param password Hash of the password
     */
    public void saveUser(String username, String password) {
        jdbcTemplate.update("INSERT INTO users (username, password) VALUES (?, ?)", username.toLowerCase(), password);
        log.info("User {} registered successfully", username);
    }

    /**
     * Deletes a user from the database and cache.
     * @param username The username of the user to delete
     */
    public void deleteUser(String username) {
        log.info("Deleting user: {}", username);
        userCache.remove(username);
        jdbcTemplate.update("DELETE FROM users WHERE username = ?", username.toLowerCase());
    }

    /**
     * Checks if a user exists in the database or cache.
     * @param username The username to check
     * @return true if the user exists, false otherwise
     */
    public boolean doesUserExist(String username) {
        log.info("Checking if user exists: {}", username);

        if (userCache.containsKey(username)) {
            log.info("User {} found in cache", username);
            return true;
        }
        log.info("User {} not found in cache, querying database", username);
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username.toLowerCase()) > 0;
    }

    public String getPasswordHash(String username) {
        log.info("Getting password hash for user: {}", username);
        return jdbcTemplate.queryForObject("SELECT password FROM users WHERE username = ?", String.class, username.toLowerCase());
    }

    /**
     * Retrieves a user from the database or cache.
     * @param username The username of the user to retrieve
     * @return The User object if found, null otherwise
     */
    public User getUser(String username) {
        log.info("Getting user: {}", username);

        if (userCache.containsKey(username)) {
            log.info("User {} found in cache", username);
            return userCache.get(username);
        }

        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE username = ?",
                    USER_ROW_MAPPER,
                    username.toLowerCase()
            );
            userCache.put(username, user);
            return user;
        } catch (Exception e) {
            log.error("Error retrieving user {}: {}", username, e.getMessage());
            return null;
        }
    }
}
