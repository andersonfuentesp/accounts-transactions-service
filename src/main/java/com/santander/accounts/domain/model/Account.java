package com.santander.accounts.domain.model;

import com.santander.accounts.domain.exception.CurrencyMismatchException;
import com.santander.accounts.domain.exception.InsufficientFundsException;
import com.santander.accounts.domain.exception.InvalidAccountDataException;
import java.time.Instant;
import java.util.UUID;

public class Account {

    private final UUID id;
    private final String owner;
    private final Currency currency;
    private Money balance;
    private long version;
    private final Instant createdAt;

    public Account(UUID id, String owner, Currency currency, Money balance,
                   long version, Instant createdAt) {
        this.id = id;
        this.owner = owner;
        this.currency = currency;
        this.balance = balance;
        this.version = version;
        this.createdAt = createdAt;
    }

    public static Account open(String owner, Currency currency, Money initialBalance) {
        if (owner == null || owner.isBlank()) {
            throw new InvalidAccountDataException("El titular es obligatorio");
        }
        if (initialBalance.isNegative()) {
            throw new InvalidAccountDataException("El saldo inicial no puede ser negativo");
        }
        if (!initialBalance.currency().equals(currency)) {
            throw new InvalidAccountDataException("El saldo inicial debe estar en la moneda de la cuenta");
        }
        return new Account(UUID.randomUUID(), owner.trim(), currency, initialBalance, 0L, Instant.now());
    }

    public void debit(Money amount) {
        if (!amount.currency().equals(currency)) {
            throw new CurrencyMismatchException("La transaccion debe usar la moneda de la cuenta: " + currency);
        }
        if (balance.isLessThan(amount)) {
            throw new InsufficientFundsException(
                    "Saldo insuficiente. Disponible: " + balance + ", solicitado: " + amount);
        }
        this.balance = balance.subtract(amount);
    }

    public void credit(Money amount) {
        if (!amount.currency().equals(currency)) {
            throw new CurrencyMismatchException("La transaccion debe usar la moneda de la cuenta: " + currency);
        }
        this.balance = balance.add(amount);
    }

    public UUID id() { return id; }
    public String owner() { return owner; }
    public Currency currency() { return currency; }
    public Money balance() { return balance; }
    public long version() { return version; }
    public Instant createdAt() { return createdAt; }
}