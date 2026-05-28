package com.santander.accounts.infrastructure.security;

import com.santander.accounts.application.port.out.UserAuthenticator;
import com.santander.accounts.infrastructure.config.AppProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ConfigUserAuthenticator implements UserAuthenticator {

    private final String username;
    private final String encodedPassword;
    private final PasswordEncoder passwordEncoder;

    public ConfigUserAuthenticator(AppProperties props, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.username = props.getAuth().getUsername();
        this.encodedPassword = passwordEncoder.encode(props.getAuth().getPassword());
    }

    @Override
    public boolean validate(String username, String rawPassword) {
        if (username == null || rawPassword == null) return false;
        return this.username.equals(username)
                && passwordEncoder.matches(rawPassword, encodedPassword);
    }
}