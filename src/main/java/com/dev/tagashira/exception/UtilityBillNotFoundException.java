package com.dev.tagashira.exception;

public class UtilityBillNotFoundException extends RuntimeException {
    public UtilityBillNotFoundException(String message) {
        super(message);
    }
    
    public UtilityBillNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
