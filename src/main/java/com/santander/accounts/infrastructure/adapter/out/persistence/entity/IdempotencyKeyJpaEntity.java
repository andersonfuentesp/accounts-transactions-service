package com.santander.accounts.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyJpaEntity {

    @Id
    @Column(name = "idempotency_key", length = 200)
    private String key;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "result_ref")
    private String resultRef;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyKeyJpaEntity() {}

    public IdempotencyKeyJpaEntity(String key, String requestHash, String status,
                                   String resultRef, Instant createdAt) {
        this.key = key;
        this.requestHash = requestHash;
        this.status = status;
        this.resultRef = resultRef;
        this.createdAt = createdAt;
    }

    public String getKey() { return key; }
    public String getRequestHash() { return requestHash; }
    public String getStatus() { return status; }
    public String getResultRef() { return resultRef; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(String status) { this.status = status; }
    public void setResultRef(String resultRef) { this.resultRef = resultRef; }
}