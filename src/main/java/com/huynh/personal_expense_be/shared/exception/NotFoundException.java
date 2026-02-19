package com.huynh.personal_expense_be.shared.exception;

public class NotFoundException extends RuntimeException {
    
    public NotFoundException(String message) {
        super(message);
    }
    
}
