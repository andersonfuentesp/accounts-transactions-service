package com.santander.accounts.infrastructure.adapter.out.persistence.entity;

import com.santander.accounts.domain.model.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions",
        indexes = @Index(name = "idx_tx_account_created", columnList = "account_id, created_at"))
public class TransactionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransactionJpaEntity() {}

    public TransactionJpaEntity(UUID id, UUID accountId, TransactionType type,
                                BigDecimal amount, String currency, Instant createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
}