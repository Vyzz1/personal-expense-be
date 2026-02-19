package com.huynh.personal_expense_be.shared.exception;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder  @AllArgsConstructor
public class ExceptionResponse {

    private String message;

    private Instant timestamp;

    private boolean success;

    public static ExceptionResponse of(String message) {
        return new ExceptionResponse(message, Instant.now(), false);
    }
}
