package com.huynh.personal_expense_be.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response for validation failures")
public record ValidationExceptionResponse(

        @Schema(description = "HTTP status code", example = "422")
        int status,

        @Schema(description = "HTTP status reason phrase", example = "Unprocessable Content")
        String error,

        @Schema(description = "Summary message", example = "Validation failed for 2 fields")
        String message,

        @Schema(description = "Request path that caused the error", example = "/api/v1/transactions")
        String path,

        @Schema(description = "Timestamp of the error")
        Instant timestamp,

        @Schema(description = "Always false for errors", example = "false")
        boolean success,

        @Schema(description = "Per-field validation errors")
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
