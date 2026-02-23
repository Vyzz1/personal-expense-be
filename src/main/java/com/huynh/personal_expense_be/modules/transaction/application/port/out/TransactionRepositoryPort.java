package com.huynh.personal_expense_be.modules.transaction.application.port.out;

import com.huynh.personal_expense_be.modules.transaction.domain.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    List<Transaction> findByUserId(String userId);

    Optional<Transaction> findById(UUID id);

    void deleteById(UUID id);


}
