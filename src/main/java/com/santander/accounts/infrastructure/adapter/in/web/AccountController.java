package com.santander.accounts.infrastructure.adapter.in.web;

import com.santander.accounts.application.port.in.CreateAccountUseCase;
import com.santander.accounts.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.santander.accounts.application.port.in.GetAccountUseCase;
import com.santander.accounts.application.port.in.ListAccountsUseCase;
import com.santander.accounts.infrastructure.adapter.in.web.dto.AccountResponse;
import com.santander.accounts.infrastructure.adapter.in.web.dto.CreateAccountRequest;
import com.santander.accounts.infrastructure.adapter.in.web.dto.PageResponse;
import com.santander.accounts.infrastructure.adapter.in.web.mapper.WebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@Validated
@Tag(name = "Cuentas")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final ListAccountsUseCase listAccountsUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase,
                             GetAccountUseCase getAccountUseCase,
                             ListAccountsUseCase listAccountsUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.listAccountsUseCase = listAccountsUseCase;
    }

    @PostMapping
    @Operation(summary = "Crea una cuenta")
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        var account = createAccountUseCase.create(
                new CreateAccountCommand(request.owner(), request.currency(), request.initialBalance()));
        return ResponseEntity
                .created(URI.create("/accounts/" + account.id()))
                .body(WebMapper.toResponse(account));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una cuenta por id")
    public AccountResponse getById(@PathVariable UUID id) {
        return WebMapper.toResponse(getAccountUseCase.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lista cuentas paginadas")
    public ResponseEntity<PageResponse<AccountResponse>> list(
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        var page = listAccountsUseCase.list(offset, limit);
        return ResponseEntity.status(HttpStatus.OK)
                .body(PageResponse.from(page, WebMapper::toResponse));
    }
}