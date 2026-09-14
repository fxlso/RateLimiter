package com.fxlso.routes;

import com.fxlso.handlers.ApiExceptionHandler;
import com.fxlso.objects.User;
import com.fxlso.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@RestController
@RequestMapping(value ="/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserRoute {

    private final UserService userService;
    public UserRoute(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody ApiExceptionHandler.RegisterNewUserRequest request) {
        userService.registerNewUser(request.username(), request.password());
        return ResponseEntity.ok(
                Map.of(
                        "message", "User registered successfully",
                        "username", request.username()
                )
        );
    }

    @DeleteMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> delete(@Valid @RequestBody ApiExceptionHandler.DeleteUserRequest request) {
        userService.deleteUser(request.username());
        return ResponseEntity.ok(
                Map.of(
                        "message", "User deleted successfully",
                        "username", request.username()
                )
        );
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody ApiExceptionHandler.LoginRequest request) {
        Map<String, Object> loginResponse = userService.login(request.username(), request.password());
        return ResponseEntity.ok(
                loginResponse
        );
    }

}
