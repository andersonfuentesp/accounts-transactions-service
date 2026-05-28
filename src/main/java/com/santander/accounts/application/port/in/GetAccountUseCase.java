package com.santander.accounts.application.port.in;

import com.santander.accounts.domain.model.Account;
import java.util.UUID;

public interface GetAccountUseCase {
    Account getById(UUID id);
}