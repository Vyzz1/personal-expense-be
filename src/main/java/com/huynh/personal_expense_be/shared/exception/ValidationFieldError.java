package com.huynh.personal_expense_be.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Details of a single field validation failure")
public record ValidationFieldError(

        @Schema(description = "Field name", example = "amount")
        String field,

        @Schema(description = "Value that was rejected", example = "-100")
        Object rejectedValue,

        @Schema(description = "Validation error message", example = "must be greater than 0")
        String message,

        @Schema(description = "Constraint annotation name", example = "Positive")
        String code
) {
    public static ValidationFieldError of(String field, Object rejectedValue, String message, String code) {
        return new ValidationFieldError(field, rejectedValue, message, code);
    }
}
