package com.dev.tagashira.exception;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(String message) {
        super(message);
    }
    
    public VehicleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
