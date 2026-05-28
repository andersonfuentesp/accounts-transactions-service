package com.santander.accounts.integration;

import com.santander.accounts.application.port.in.CreateAccountUseCase;
import com.santander.accounts.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.santander.accounts.application.port.in.CreateTransactionUseCase;
import com.santander.accounts.application.port.in.CreateTransactionUseCase.CreateTransactionCommand;
import com.santander.accounts.application.port.in.GetAccountUseCase;
import com.santander.accounts.domain.model.Account;
import com.santander.accounts.domain.model.Transaction;
import com.santander.accounts.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IdempotencyIntegrationTest {

    @Autowired CreateAccountUseCase createAccount;
    @Autowired CreateTransactionUseCase createTransaction;
    @Autowired GetAccountUseCase getAccount;

    @Test
    void misma_idempotency_key_no_duplica_la_transaccion() {
        Account account = createAccount.create(
                new CreateAccountCommand("Anderson", "PEN", new BigDecimal("100.00")));

        var command = new CreateTransactionCommand(
                "key-fija-001", account.id(), TransactionType.DEBIT,
                new BigDecimal("30.00"), "PEN");

        Transaction first = createTransaction.create(command);
        Transaction second = createTransaction.create(command); // reintento idéntico

        // Misma transacción devuelta, no una nueva
        assertEquals(first.id(), second.id());

        // El saldo se debitó UNA sola vez (100 - 30 = 70)
        Account after = getAccount.getById(account.id());
        assertEquals(new BigDecimal("70.00"), after.balance().amount());
    }

    @Test
    void misma_key_con_cuerpo_distinto_es_conflicto() {
        Account account = createAccount.create(
                new CreateAccountCommand("Anderson", "PEN", new BigDecimal("100.00")));

        createTransaction.create(new CreateTransactionCommand(
                "key-fija-002", account.id(), TransactionType.DEBIT,
                new BigDecimal("10.00"), "PEN"));

        // Misma key, monto distinto -> debe fallar
        assertThrows(RuntimeException.class, () -> createTransaction.create(
                new CreateTransactionCommand(
                        "key-fija-002", account.id(), TransactionType.DEBIT,
                        new BigDecimal("99.00"), "PEN")));
    }
}