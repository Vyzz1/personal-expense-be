package com.huynh.personal_expense_be.modules.transaction.application.dto;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
        return new PageResult<>(content, page, size, totalElements, totalPages, last);
    }
}
