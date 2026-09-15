package com.fxlso.repositories;

import com.fxlso.objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
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

    public void deleteUser(String username) {
        jdbcTemplate.update("DELETE FROM users WHERE username = ?", username.toLowerCase());
    }

    public boolean doesUserExist(String username) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username.toLowerCase()) > 0;
    }

    public String getPasswordHash(String username) {
        return jdbcTemplate.queryForObject("SELECT password FROM users WHERE username = ?", String.class, username.toLowerCase());
    }

    public User getUser(String username) {
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE username = ?", User.class, username.toLowerCase());
    }
}
