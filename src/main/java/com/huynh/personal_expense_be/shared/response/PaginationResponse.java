package com.huynh.personal_expense_be.shared.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponse<T> {

    private List<T> content;

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

     public static <T> PaginationResponse<T> of(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
        return new PaginationResponse<>(content, page, size, totalElements, totalPages, last);
    }

}
