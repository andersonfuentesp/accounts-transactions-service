package com.santander.accounts.application.port.in;

public interface AuthenticateUseCase {
    String login(LoginCommand command);

    record LoginCommand(String username, String password) {}
}