package com.huynh.personal_expense_be.shared.exception;

public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
}
