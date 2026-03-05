package com.huynh.personal_expense_be.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidationExceptionResponse(

        int status,

        String error,

        String message,

        String path,

        Instant timestamp,

        boolean success,

        List<ValidationFieldError> fieldErrors
) {
    public static ValidationExceptionResponse of(int status,
                                                  String error,
                                                  String message,
                                                  String path,
                                                  List<ValidationFieldError> fieldErrors) {
        return new ValidationExceptionResponse(
                status,
                error,
                message,
                path,
                Instant.now(),
                false,
                fieldErrors
        );
    }
}
