package com.app.coworking.exception;

/**
 * Исключение, которое оборачивает все ошибки, возникшие при вызове контроллера через аспект.
 */
public class ControllerInvocationException extends RuntimeException {

    public ControllerInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

}
