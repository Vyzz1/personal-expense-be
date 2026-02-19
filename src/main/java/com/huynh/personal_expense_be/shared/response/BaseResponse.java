package com.huynh.personal_expense_be.shared.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class BaseResponse <T> {

    private String message;

    private boolean success;
    
    private T data;

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(message, true, data);
    }

}
