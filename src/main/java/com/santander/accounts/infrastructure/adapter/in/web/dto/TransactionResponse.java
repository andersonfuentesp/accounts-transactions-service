package com.santander.accounts.infrastructure.adapter.in.web.dto;

import com.santander.accounts.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        Instant createdAt) {}