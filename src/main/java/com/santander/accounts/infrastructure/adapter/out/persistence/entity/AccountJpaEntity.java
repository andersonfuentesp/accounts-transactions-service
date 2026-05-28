package com.santander.accounts.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Transient
    private boolean isNew = true;

    protected AccountJpaEntity() {}

    public AccountJpaEntity(UUID id, String owner, String currency, BigDecimal balance,
                            long version, Instant createdAt) {
        this.id = id;
        this.owner = owner;
        this.currency = currency;
        this.balance = balance;
        this.version = version;
        this.createdAt = createdAt;
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return isNew; }

    public void markNotNew() { this.isNew = false; }

    @PostLoad
    @PostPersist
    void trackPersisted() { this.isNew = false; }

    public String getOwner() { return owner; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }

    public void setOwner(String owner) { this.owner = owner; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}