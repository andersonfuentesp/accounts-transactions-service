package com.santander.accounts.application.port.in;

import com.santander.accounts.domain.model.Transaction;
import com.santander.accounts.domain.model.TransactionType;
import java.math.BigDecimal;
import java.util.UUID;

public interface CreateTransactionUseCase {
    Transaction create(CreateTransactionCommand command);

    record CreateTransactionCommand(
            String idempotencyKey,
            UUID accountId,
            TransactionType type,
            BigDecimal amount,
            String currency) {}
}