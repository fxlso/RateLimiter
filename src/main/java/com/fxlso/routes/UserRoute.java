package com.fxlso.routes;

import com.fxlso.exceptions.InvalidCredentialsException;
import com.fxlso.repositories.UserRepository;
import com.fxlso.requests.LoginRequest;
import com.fxlso.requests.RegisterNewUserRequest;
import com.fxlso.services.JwtService;
import com.fxlso.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping(value ="/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserRoute {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public UserRoute(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterNewUserRequest request) {
        userService.registerNewUser(request.username(), request.password());
        return ResponseEntity.ok(
                Map.of(
                        "message", "User registered successfully",
                        "username", request.username()
                )
        );
    }

    @DeleteMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> delete() {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.deleteUser(authenticatedUsername);
        return ResponseEntity.ok(
                Map.of(
                        "message", "User deleted successfully",
                        "username", authenticatedUsername
                )
        );
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken
                        (request.username(), request.password())
        );
        if (authentication.isAuthenticated()) {
            return ResponseEntity.ok(
                Map.of(
                    "message", "Login successful",
                    "token", jwtService.generateToken(request.username())
                )
            );
        } else {
            throw new InvalidCredentialsException("Invalid user request!");
        }
    }

    @DeleteMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // TODO: make refresh token system + db verification to ensure that logouts expire tokens and long sign-ins work better
        if (auth == null) {
            return ResponseEntity.status(401).body(
                    Map.of(
                            "error", "Unauthorized",
                            "message", "No user is currently authenticated"
                    )
            );
        }

        auth.setAuthenticated(false);
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(
                Map.of(
                        "message", "Logout successful"
                )
        );
    }
}
