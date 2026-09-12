package com.fxlso.objects;

import java.util.UUID;

public record User(String username, String id) {

    public User(String name) {
        this(name, UUID.randomUUID().toString());
    }

}

