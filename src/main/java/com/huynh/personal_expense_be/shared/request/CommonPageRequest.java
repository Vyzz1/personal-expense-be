package com.huynh.personal_expense_be.shared.request;

import lombok.Data;

@Data
public class CommonPageRequest {

    private int page = 0;
    private int size = 30;
    private String sortBy = "createdAt";
    private String sortOrder = "desc";
}
