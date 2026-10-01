package com.fxlso.routes;

import com.fxlso.exceptions.InvalidCredentialsException;
import com.fxlso.objects.User;
import com.fxlso.repositories.UserRepository;
import com.fxlso.requests.LoginRequest;
import com.fxlso.requests.RegisterNewUserRequest;
import com.fxlso.services.JwtService;
import com.fxlso.services.TokenService;
import com.fxlso.requests.RefreshTokenRequest;
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
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public UserRoute(UserService userService, AuthenticationManager authenticationManager, TokenService tokenService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
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
                    "tokens", tokenService.issue(request.username())
                )
            );
        } else {
            throw new InvalidCredentialsException("Invalid user request!");
        }
    }

    @GetMapping(value ="/profile")
    public ResponseEntity<Map<String, Object>> profile() {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userService.loadUserByUsername(authenticatedUsername);
        return ResponseEntity.ok(
                Map.of(
                        "username", user.username(),
                        "id", user.id()
                )
        );
    }

    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(Map.of("tokens", tokenService.refresh(request.refreshToken())));
    }

    @DeleteMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        tokenService.revoke(request.refreshToken());
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
