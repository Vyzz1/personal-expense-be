package com.huynh.personal_expense_be.shared.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Error response for domain exceptions")
public class ExceptionResponse {

    @Schema(description = "Error message", example = "Resource not found")
    private String message;

    @Schema(description = "Timestamp of the error")
    private Instant timestamp;

    @Schema(description = "Always false for errors", example = "false")
    private boolean success;

    public static ExceptionResponse of(String message) {
        return new ExceptionResponse(message, Instant.now(), false);
    }
}
