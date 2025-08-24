package com.kubrafelek.brokagefirm.exception;

public class InvalidAssetException extends RuntimeException {
    public InvalidAssetException(String message) {
        super(message);
    }

    public InvalidAssetException(String message, Throwable cause) {
        super(message, cause);
    }
}
