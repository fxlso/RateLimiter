package com.fxlso.requests;

import jakarta.validation.constraints.NotBlank;

public record DeleteUserRequest(@NotBlank String username) {}