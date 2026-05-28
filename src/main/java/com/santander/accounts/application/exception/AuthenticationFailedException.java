package com.santander.accounts.application.exception;

public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) { super(message); }
}