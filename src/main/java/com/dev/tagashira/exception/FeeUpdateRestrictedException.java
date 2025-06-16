package com.dev.tagashira.exception;

public class FeeUpdateRestrictedException extends RuntimeException {
    
    public FeeUpdateRestrictedException(String message) {
        super(message);
    }
    
    public FeeUpdateRestrictedException(String message, Throwable cause) {
        super(message, cause);
    }
} 