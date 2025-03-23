package me.adversing.nihil.exception;

public class PropertyUpdateException extends RuntimeException {

    public PropertyUpdateException(String message) {
        super(message);
    }

    public PropertyUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}