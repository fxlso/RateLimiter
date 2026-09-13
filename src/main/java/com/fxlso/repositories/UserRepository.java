package com.fxlso.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
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
        jdbcTemplate.update("INSERT INTO users (username, password) VALUES (?, ?)", username, password);
    }

    public boolean doesUserExist(String username) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username) > 0;
    }
}
