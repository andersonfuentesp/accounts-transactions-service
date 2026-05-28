package com.santander.accounts.application.port.in;

import com.santander.accounts.domain.model.Account;

public interface ListAccountsUseCase {
    PageResult<Account> list(int offset, int limit);
}