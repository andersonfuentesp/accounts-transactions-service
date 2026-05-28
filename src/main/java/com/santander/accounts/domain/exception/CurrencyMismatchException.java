package com.santander.accounts.domain.exception;

public class CurrencyMismatchException extends DomainException {
    public CurrencyMismatchException(String message) { super(message); }
}