package com.santander.accounts.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank(message = "owner es obligatorio")
        String owner,

        @NotBlank(message = "currency es obligatorio")
        String currency,

        @NotNull(message = "initialBalance es obligatorio")
        @DecimalMin(value = "0.0", message = "initialBalance no puede ser negativo")
        BigDecimal initialBalance) {}