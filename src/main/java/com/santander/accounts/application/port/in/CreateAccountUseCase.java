package com.santander.accounts.application.port.in;

import com.santander.accounts.domain.model.Account;
import java.math.BigDecimal;

public interface CreateAccountUseCase {
    Account create(CreateAccountCommand command);

    record CreateAccountCommand(String owner, String currency, BigDecimal initialBalance) {}
}