package com.santander.accounts.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final UUID accountId;
    private final TransactionType type;
    private final Money amount;
    private final Instant createdAt;

    public Transaction(UUID id, UUID accountId, TransactionType type, Money amount, Instant createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public static Transaction create(UUID accountId, TransactionType type, Money amount) {
        return new Transaction(UUID.randomUUID(), accountId, type, amount, Instant.now());
    }

    public UUID id() { return id; }
    public UUID accountId() { return accountId; }
    public TransactionType type() { return type; }
    public Money amount() { return amount; }
    public Instant createdAt() { return createdAt; }
}