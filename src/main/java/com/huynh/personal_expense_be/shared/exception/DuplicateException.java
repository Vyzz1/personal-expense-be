package com.huynh.personal_expense_be.shared.exception;

public class DuplicateException extends RuntimeException {
    
    public DuplicateException(String message) {
        super(message);
    }
}
