package com.app.coworking.exception;

public class LoggingException extends RuntimeException {
    public LoggingException(String message) {
        super(message);
    }
}