package com.fxlso.routes;

import com.fxlso.objects.User;
import com.fxlso.services.KeyService;
import com.fxlso.services.UserService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value ="/apiKey", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiKeyRoute {

    private final KeyService keyService;
    private final UserService userService;
    public ApiKeyRoute(KeyService keyService, UserService userService) {
        this.keyService = keyService;
        this.userService = userService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateApiKey() {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userService.loadUserByUsername(authenticatedUsername);
        String apiKey = keyService.generateKey(user);

        // TODO Accept request parameters for max_requests and limit_reset_time_ms, and pass them to the generateKey method.
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "apiKey", apiKey,
                        "message", "API key generated successfully",
                        "request_limit", 1000,
                        "limit_reset_time_ms", 60 * 60 * 1000
                )
        );

    }

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getApiKeyDetails() {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userService.loadUserByUsername(authenticatedUsername);
        Map<String, Object> apiKeyDetails = keyService.getApiKeyDetails(user);

        return ResponseEntity.ok(apiKeyDetails);
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<Map<String, Object>> revokeApiKey() {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userService.loadUserByUsername(authenticatedUsername);
        keyService.revokeKeys(user);
        return ResponseEntity.ok(Map.of("message", "If there was an active API key, it has been revoked."));
    }
}