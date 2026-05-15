package com.huynh.personal_expense_be.shared.exception;

public class SecurityConfigurationException extends RuntimeException {

    public SecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}