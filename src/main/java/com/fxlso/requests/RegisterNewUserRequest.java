package com.fxlso.requests;

import jakarta.validation.constraints.NotBlank;

public record RegisterNewUserRequest(@NotBlank String username, @NotBlank String password) {}