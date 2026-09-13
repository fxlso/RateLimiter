package com.fxlso.services;

import com.fxlso.exceptions.MissingInformationException;
import com.fxlso.exceptions.UserAlreadyExistsException;
import com.fxlso.repositories.UserRepository;
import com.fxlso.util.PasswordUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;

@Service
public class UserService {


    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user.
     * @param username 255 characters
     * @param password Hash of the password
     */
    public void registerNewUser(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new MissingInformationException("Username cannot be null or empty");
        }

        if (password == null || password.isEmpty()) {
            throw new MissingInformationException("Password cannot be null or empty");
        }

        if (userRepository.doesUserExist(username)) {
            throw new UserAlreadyExistsException("A user with this username already exists");
        }

        password = PasswordUtil.hashPassword(password);

        userRepository.saveUser(username, password);
    }

}
