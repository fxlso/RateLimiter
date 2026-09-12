package com.fxlso.routes;

import com.fxlso.objects.User;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value ="/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserRoute {

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> register(@RequestBody User newUser) {
        // Create a new User instance with the provided username
        User user = new User(newUser.username());

        return ResponseEntity.ok(
                Map.of(
                        "message", "User registered successfully",
                        "id", user.id(),
                        "username", user.username()
                )
        );
    }

}
