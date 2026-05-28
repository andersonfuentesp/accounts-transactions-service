package com.santander.accounts.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String owner,
        String currency,
        BigDecimal balance,
        long version,
        Instant createdAt) {}