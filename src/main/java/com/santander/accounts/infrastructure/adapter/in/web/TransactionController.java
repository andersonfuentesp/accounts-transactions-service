package com.santander.accounts.infrastructure.adapter.in.web;

import com.santander.accounts.application.port.in.CreateTransactionUseCase;
import com.santander.accounts.application.port.in.CreateTransactionUseCase.CreateTransactionCommand;
import com.santander.accounts.application.port.in.ListTransactionsUseCase;
import com.santander.accounts.application.port.in.ListTransactionsUseCase.ListTransactionsQuery;
import com.santander.accounts.domain.model.TransactionType;
import com.santander.accounts.infrastructure.adapter.in.web.dto.CreateTransactionRequest;
import com.santander.accounts.infrastructure.adapter.in.web.dto.PageResponse;
import com.santander.accounts.infrastructure.adapter.in.web.dto.TransactionResponse;
import com.santander.accounts.infrastructure.adapter.in.web.mapper.WebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@RestController
@Validated
@Tag(name = "Transacciones")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase,
                                 ListTransactionsUseCase listTransactionsUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.listTransactionsUseCase = listTransactionsUseCase;
    }

    @PostMapping("/transactions")
    @Operation(summary = "Crea una transacción (idempotente)")
    public ResponseEntity<TransactionResponse> create(
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "El header Idempotency-Key es obligatorio") String idempotencyKey,
            @Valid @RequestBody CreateTransactionRequest request) {

        var tx = createTransactionUseCase.create(new CreateTransactionCommand(
                idempotencyKey, request.accountId(), request.type(),
                request.amount(), request.currency()));

        return ResponseEntity
                .created(URI.create("/transactions/" + tx.id()))
                .body(WebMapper.toResponse(tx));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    @Operation(summary = "Lista movimientos de una cuenta con filtros")
    public PageResponse<TransactionResponse> list(
            @PathVariable UUID accountId,
            @Parameter(description = "Desde (ISO-8601, ej. 2026-01-01T00:00:00Z)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "Hasta (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {

        var page = listTransactionsUseCase.list(
                new ListTransactionsQuery(accountId, from, to, type, offset, limit));
        return PageResponse.from(page, WebMapper::toResponse);
    }
}