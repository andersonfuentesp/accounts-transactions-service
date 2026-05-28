package com.santander.accounts.infrastructure.adapter.in.web.mapper;

import com.santander.accounts.domain.model.Account;
import com.santander.accounts.domain.model.Transaction;
import com.santander.accounts.infrastructure.adapter.in.web.dto.AccountResponse;
import com.santander.accounts.infrastructure.adapter.in.web.dto.TransactionResponse;

public final class WebMapper {

    private WebMapper() {}

    public static AccountResponse toResponse(Account a) {
        return new AccountResponse(a.id(), a.owner(), a.currency().code(),
                a.balance().amount(), a.version(), a.createdAt());
    }

    public static TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(t.id(), t.accountId(), t.type(),
                t.amount().amount(), t.amount().currency().code(), t.createdAt());
    }
}