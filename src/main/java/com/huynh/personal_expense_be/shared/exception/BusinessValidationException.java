package com.huynh.personal_expense_be.shared.exception;

import java.util.List;

public class BusinessValidationException extends RuntimeException {

    private final List<ValidationFieldError> errors;

    public BusinessValidationException(List<ValidationFieldError> errors) {
        this.errors = errors;
    }

    public List<ValidationFieldError> getErrors() {
        return errors;
    }
}
