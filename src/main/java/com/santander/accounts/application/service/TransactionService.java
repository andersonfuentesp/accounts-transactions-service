package com.santander.accounts.application.service;

import com.santander.accounts.application.exception.ConcurrencyConflictException;
import com.santander.accounts.application.exception.IdempotencyConflictException;
import com.santander.accounts.application.port.in.CreateTransactionUseCase;
import com.santander.accounts.application.port.in.ListTransactionsUseCase;
import com.santander.accounts.application.port.in.PageResult;
import com.santander.accounts.application.port.out.IdempotencyRecord;
import com.santander.accounts.application.port.out.IdempotencyStore;
import com.santander.accounts.application.port.out.TransactionRepository;
import com.santander.accounts.domain.model.Transaction;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService implements CreateTransactionUseCase, ListTransactionsUseCase {

    private static final int MAX_RETRIES = 3;

    private final TransactionPersister persister;
    private final IdempotencyStore idempotencyStore;
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionPersister persister,
                              IdempotencyStore idempotencyStore,
                              TransactionRepository transactionRepository) {
        this.persister = persister;
        this.idempotencyStore = idempotencyStore;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction create(CreateTransactionCommand command) {
        String requestHash = hash(command);

        // 1) Registro idempotente atómico
        Optional<IdempotencyRecord> existing =
                idempotencyStore.tryBegin(command.idempotencyKey(), requestHash);

        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (!record.requestHash().equals(requestHash)) {
                throw new IdempotencyConflictException(
                        "La Idempotency-Key ya fue usada con un cuerpo distinto");
            }
            if (IdempotencyRecord.COMPLETED.equals(record.status()) && record.resultRef() != null) {
                // Reintento del cliente: devolvemos la transacción original
                return transactionRepository.findById(UUID.fromString(record.resultRef()))
                        .orElseThrow(() -> new IdempotencyConflictException(
                                "Resultado idempotente no encontrado"));
            }
            // misma key + mismo cuerpo, aún en proceso
            throw new IdempotencyConflictException(
                    "Petición con la misma Idempotency-Key en curso");
        }

        // 2) Ejecuta con reintento ante conflicto de versión
        try {
            Transaction result = executeWithRetry(command);
            idempotencyStore.complete(command.idempotencyKey(), result.id().toString());
            return result;
        } catch (RuntimeException ex) {
            // Falló (saldo insuficiente, etc.): liberamos la clave para permitir corrección
            idempotencyStore.remove(command.idempotencyKey());
            throw ex;
        }
    }

    private Transaction executeWithRetry(CreateTransactionCommand command) {
        int attempts = 0;
        while (true) {
            try {
                return persister.execute(command);
            } catch (OptimisticLockingFailureException ex) {
                if (++attempts >= MAX_RETRIES) {
                    throw new ConcurrencyConflictException(
                            "No se pudo completar la transacción por concurrencia tras "
                                    + MAX_RETRIES + " intentos");
                }
                // reintenta: el persister recargará la cuenta con la versión actualizada
            }
        }
    }

    @Override
    public PageResult<Transaction> list(ListTransactionsQuery q) {
        var items = transactionRepository.findByAccount(
                q.accountId(), q.from(), q.to(), q.type(), q.offset(), q.limit());
        long total = transactionRepository.countByAccount(
                q.accountId(), q.from(), q.to(), q.type());
        return new PageResult<>(items, q.offset(), q.limit(), total);
    }

    /** Hash estable del contenido de la petición, para detectar reuso de la clave. */
    private String hash(CreateTransactionCommand c) {
        String raw = c.accountId() + "|" + c.type() + "|"
                + c.amount().stripTrailingZeros().toPlainString() + "|" + c.currency();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de idempotencia", e);
        }
    }
}