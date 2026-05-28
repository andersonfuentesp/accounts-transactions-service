package com.santander.accounts.application.port.in;

import com.santander.accounts.domain.model.Transaction;
import com.santander.accounts.domain.model.TransactionType;
import java.time.Instant;
import java.util.UUID;

public interface ListTransactionsUseCase {
    PageResult<Transaction> list(ListTransactionsQuery query);

    record ListTransactionsQuery(
            UUID accountId, Instant from, Instant to,
            TransactionType type, int offset, int limit) {}
}