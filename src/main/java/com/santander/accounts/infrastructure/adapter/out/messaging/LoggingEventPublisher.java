package com.santander.accounts.infrastructure.adapter.out.messaging;

import com.santander.accounts.application.port.out.EventPublisher;
import com.santander.accounts.domain.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisher.class);

    @Override
    public void publishTransactionCreated(Transaction transaction) {
        log.info("event=TransactionCreated transactionId={} accountId={} type={}",
                transaction.id(), transaction.accountId(), transaction.type());
    }
}