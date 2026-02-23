package com.huynh.personal_expense_be.modules.transaction.application.port.in;

import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionResponse;

import java.util.List;

public interface GetListTransactionUseCase {

    List<TransactionResponse> getListTransaction(String userId);
}
