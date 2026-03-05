package com.huynh.personal_expense_be.modules.transaction.application.port.in;

import com.huynh.personal_expense_be.modules.transaction.application.dto.GetTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.PageResult;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionResponse;


public interface GetListTransactionUseCase {

    PageResult<TransactionResponse> getListTransaction(GetTransactionCommand command);
}
