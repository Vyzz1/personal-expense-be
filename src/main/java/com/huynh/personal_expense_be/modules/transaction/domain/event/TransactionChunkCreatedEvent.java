package com.huynh.personal_expense_be.modules.transaction.domain.event;

import java.util.List;

public record TransactionChunkCreatedEvent (
        List<TransactionCreatedEvent> events
){
}
