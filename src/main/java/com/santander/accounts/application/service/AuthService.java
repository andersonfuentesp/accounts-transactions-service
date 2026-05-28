package com.santander.accounts.application.service;

import com.santander.accounts.application.exception.AuthenticationFailedException;
import com.santander.accounts.application.port.in.AuthenticateUseCase;
import com.santander.accounts.application.port.out.TokenProvider;
import com.santander.accounts.application.port.out.UserAuthenticator;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AuthenticateUseCase {

    private final UserAuthenticator userAuthenticator;
    private final TokenProvider tokenProvider;

    public AuthService(UserAuthenticator userAuthenticator, TokenProvider tokenProvider) {
        this.userAuthenticator = userAuthenticator;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public String login(LoginCommand command) {
        if (!userAuthenticator.validate(command.username(), command.password())) {
            throw new AuthenticationFailedException("Credenciales inválidas");
        }
        return tokenProvider.issueToken(command.username());
    }
}