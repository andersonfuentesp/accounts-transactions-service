package com.santander.accounts.infrastructure.adapter.in.web.dto;

import com.santander.accounts.domain.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull(message = "accountId es obligatorio")
        UUID accountId,

        @NotNull(message = "type es obligatorio (DEBIT o CREDIT)")
        TransactionType type,

        @NotNull(message = "amount es obligatorio")
        @DecimalMin(value = "0.01", message = "amount debe ser mayor a 0")
        BigDecimal amount,

        @NotBlank(message = "currency es obligatorio")
        String currency) {}