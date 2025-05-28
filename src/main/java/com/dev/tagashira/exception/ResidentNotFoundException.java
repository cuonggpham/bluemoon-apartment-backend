package com.dev.tagashira.exception;

public class ResidentNotFoundException extends RuntimeException {
    public ResidentNotFoundException(String message) {
        super(message);
    }
    
    public ResidentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
