package com.santander.accounts.application.port.out;

import com.santander.accounts.domain.model.Transaction;

public interface EventPublisher {
    void publishTransactionCreated(Transaction transaction);
}