package com.dev.tagashira.exception;

public class NoVehicleException extends Throwable {
    public NoVehicleException(Long addressNumber) {
        super("No vehicle found at address number: " + addressNumber);
    }
}
