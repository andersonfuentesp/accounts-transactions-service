package com.santander.accounts.application.port.out;

import com.santander.accounts.domain.model.Account;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    List<Account> findAll(int offset, int limit);
    long count();
}