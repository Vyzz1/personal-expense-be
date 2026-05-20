package com.huynh.personal_expense_be.shared.exception;

public class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException() {
        super("An unexpected error occurred. Please try again later.");
    }
}
