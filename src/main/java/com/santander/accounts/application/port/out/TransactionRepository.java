package com.santander.accounts.application.port.out;

import com.santander.accounts.domain.model.Transaction;
import com.santander.accounts.domain.model.TransactionType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    List<Transaction> findByAccount(UUID accountId, Instant from, Instant to,
                                    TransactionType type, int offset, int limit);
    long countByAccount(UUID accountId, Instant from, Instant to, TransactionType type);
}