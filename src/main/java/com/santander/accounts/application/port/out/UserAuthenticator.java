package com.santander.accounts.application.port.out;

public interface UserAuthenticator {
    boolean validate(String username, String rawPassword);
}