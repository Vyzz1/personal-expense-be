package com.huynh.personal_expense_be.modules.transaction.domain.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BatchJob {
    private String batchId;
    private String status;
}
