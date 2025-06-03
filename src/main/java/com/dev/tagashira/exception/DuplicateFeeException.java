package com.dev.tagashira.exception;

public class DuplicateFeeException extends Throwable {
    public DuplicateFeeException(String feeName, Long addressNumber) {
        super(String.format("Phí '%s' đã tồn tại cho địa chỉ số %d", feeName, addressNumber));
    }
}
