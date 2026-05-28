package com.santander.accounts.application.service;

import com.santander.accounts.application.port.in.CreateTransactionUseCase.CreateTransactionCommand;
import com.santander.accounts.application.port.out.AccountRepository;
import com.santander.accounts.application.port.out.EventPublisher;
import com.santander.accounts.application.port.out.TransactionRepository;
import com.santander.accounts.domain.exception.AccountNotFoundException;
import com.santander.accounts.domain.model.Account;
import com.santander.accounts.domain.model.Currency;
import com.santander.accounts.domain.model.Money;
import com.santander.accounts.domain.model.Transaction;
import com.santander.accounts.domain.model.TransactionType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionPersister {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EventPublisher eventPublisher;

    public TransactionPersister(AccountRepository accountRepository,
                                TransactionRepository transactionRepository,
                                EventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Ejecuta el movimiento dentro de su propia transacción. Si otra transacción
     * concurrente modificó la cuenta, el commit fallará con
     * OptimisticLockingFailureException y el orquestador reintentará.
     */
    @Transactional
    public Transaction execute(CreateTransactionCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Cuenta no encontrada: " + command.accountId()));

        Currency currency = Currency.of(command.currency());
        Money amount = Money.of(command.amount(), currency);

        if (command.type() == TransactionType.DEBIT) {
            account.debit(amount);   // valida moneda y saldo dentro del dominio
        } else {
            account.credit(amount);
        }
        accountRepository.save(account); // verifica @Version (bloqueo optimista)

        Transaction tx = Transaction.create(account.id(), command.type(), amount);
        Transaction saved = transactionRepository.save(tx);
        eventPublisher.publishTransactionCreated(saved);
        return saved;
    }
}