package com.huynh.personal_expense_be.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Schema(description = "Standard API response wrapper")
public class BaseResponse<T> {

    @Schema(description = "Human-readable message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Whether the operation succeeded", example = "true")
    private boolean success;

    @Schema(description = "Response payload")
    private T data;

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(message, true, data);
    }

    public static <T> BaseResponse<T> noData(String message) {
        return new BaseResponse<>(message, true, null);
    }
}
