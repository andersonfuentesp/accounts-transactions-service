package com.santander.accounts.integration;

import com.santander.accounts.application.port.in.CreateAccountUseCase;
import com.santander.accounts.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.santander.accounts.application.port.in.CreateTransactionUseCase;
import com.santander.accounts.application.port.in.CreateTransactionUseCase.CreateTransactionCommand;
import com.santander.accounts.application.port.in.GetAccountUseCase;
import com.santander.accounts.domain.model.Account;
import com.santander.accounts.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConcurrencyIntegrationTest {

    @Autowired CreateAccountUseCase createAccount;
    @Autowired CreateTransactionUseCase createTransaction;
    @Autowired GetAccountUseCase getAccount;

    @Test
    void dos_debitos_simultaneos_no_dejan_saldo_negativo() throws Exception {
        // Cuenta con saldo para UN solo débito de 80
        Account account = createAccount.create(
                new CreateAccountCommand("Anderson", "PEN", new BigDecimal("100.00")));
        UUID accountId = account.id();

        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger fallos = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch arranque = new CountDownLatch(1);

        Callable<Void> debito = () -> {
            arranque.await(); // que ambos hilos partan a la vez
            try {
                createTransaction.create(new CreateTransactionCommand(
                        UUID.randomUUID().toString(), accountId, TransactionType.DEBIT,
                        new BigDecimal("80.00"), "PEN"));
                exitos.incrementAndGet();
            } catch (RuntimeException e) {
                fallos.incrementAndGet(); // saldo insuficiente o conflicto
            }
            return null;
        };

        Future<Void> f1 = pool.submit(debito);
        Future<Void> f2 = pool.submit(debito);
        arranque.countDown(); // ¡ya!
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        pool.shutdown();

        // Exactamente uno debe haber tenido éxito
        assertEquals(1, exitos.get(), "Solo un débito debe completarse");
        assertEquals(1, fallos.get(), "El otro débito debe fallar");

        // El saldo nunca queda negativo (100 - 80 = 20)
        Account after = getAccount.getById(accountId);
        assertEquals(new BigDecimal("20.00"), after.balance().amount());
        assertFalse(after.balance().isNegative());
    }
}