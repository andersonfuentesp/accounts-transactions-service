package com.santander.accounts.infrastructure.adapter.out.persistence;

import com.santander.accounts.domain.model.Account;
import com.santander.accounts.domain.model.Currency;
import com.santander.accounts.domain.model.Money;
import com.santander.accounts.domain.model.Transaction;
import com.santander.accounts.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.santander.accounts.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;

public final class PersistenceMapper {

    private PersistenceMapper() {}

    public static AccountJpaEntity toEntity(Account a) {
        return new AccountJpaEntity(a.id(), a.owner(), a.currency().code(),
                a.balance().amount(), a.version(), a.createdAt());
    }

    public static Account toDomain(AccountJpaEntity e) {
        Currency currency = Currency.of(e.getCurrency());
        return new Account(e.getId(), e.getOwner(), currency,
                Money.of(e.getBalance(), currency), e.getVersion(), e.getCreatedAt());
    }

    public static TransactionJpaEntity toEntity(Transaction t) {
        return new TransactionJpaEntity(t.id(), t.accountId(), t.type(),
                t.amount().amount(), t.amount().currency().code(), t.createdAt());
    }

    public static Transaction toDomain(TransactionJpaEntity e) {
        Currency currency = Currency.of(e.getCurrency());
        return new Transaction(e.getId(), e.getAccountId(), e.getType(),
                Money.of(e.getAmount(), currency), e.getCreatedAt());
    }
}