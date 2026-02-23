package com.huynh.personal_expense_be.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;


@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidationFieldError(

        String field,

        Object rejectedValue,

        String message,

        String code
) {
    public static ValidationFieldError of(String field, Object rejectedValue, String message, String code) {
        return new ValidationFieldError(field, rejectedValue, message, code);
    }
}
