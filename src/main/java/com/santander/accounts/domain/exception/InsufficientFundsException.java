package com.santander.accounts.domain.exception;

public class InsufficientFundsException extends DomainException {
    public InsufficientFundsException(String message) { super(message); }
}