package com.fxlso.exceptions;

public class UnauthenticatedDeletionRequest extends RuntimeException {
    public UnauthenticatedDeletionRequest(String message) {
        super(message);
    }
}
