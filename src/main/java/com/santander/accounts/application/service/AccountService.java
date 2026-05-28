package com.santander.accounts.application.service;

import com.santander.accounts.application.port.in.CreateAccountUseCase;
import com.santander.accounts.application.port.in.GetAccountUseCase;
import com.santander.accounts.application.port.in.ListAccountsUseCase;
import com.santander.accounts.application.port.in.PageResult;
import com.santander.accounts.application.port.out.AccountRepository;
import com.santander.accounts.domain.exception.AccountNotFoundException;
import com.santander.accounts.domain.model.Account;
import com.santander.accounts.domain.model.Currency;
import com.santander.accounts.domain.model.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService implements CreateAccountUseCase, GetAccountUseCase, ListAccountsUseCase {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public Account create(CreateAccountCommand command) {
        Currency currency = Currency.of(command.currency());
        Money initialBalance = Money.of(command.initialBalance(), currency);
        Account account = Account.open(command.owner(), currency, initialBalance);
        return accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Account> list(int offset, int limit) {
        var items = accountRepository.findAll(offset, limit);
        long total = accountRepository.count();
        return new PageResult<>(items, offset, limit, total);
    }
}