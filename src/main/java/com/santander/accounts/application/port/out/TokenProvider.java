package com.santander.accounts.application.port.out;

public interface TokenProvider {
    String issueToken(String subject);
}