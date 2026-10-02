package com.fxlso.routes;

import com.fxlso.objects.User;
import com.fxlso.requests.ApiKeyGenRequest;
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
    public ResponseEntity<Map<String, Object>> generateApiKey(@RequestBody(required = false) ApiKeyGenRequest apiKeyGenRequest) {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userService.loadUserByUsername(authenticatedUsername);

        Map<String, Object> res;
        if (apiKeyGenRequest == null) {
            res = keyService.generateKey(user);
        } else {
            res = keyService.generateKey(user, apiKeyGenRequest.requestLimit(), apiKeyGenRequest.limitResetMs());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
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