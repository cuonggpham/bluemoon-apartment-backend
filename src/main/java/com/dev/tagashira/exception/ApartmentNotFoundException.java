package com.dev.tagashira.exception;

public class ApartmentNotFoundException extends RuntimeException {
    public ApartmentNotFoundException(String message) {
        super(message);
    }
    
    public ApartmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
