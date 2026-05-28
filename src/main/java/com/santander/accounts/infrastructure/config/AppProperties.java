package com.santander.accounts.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Auth auth = new Auth();

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    public Auth getAuth() { return auth; }
    public void setAuth(Auth auth) { this.auth = auth; }

    public static class Jwt {
        private String issuer = "accounts-transactions-service";
        private String audience = "accounts-clients";
        private long expirationMinutes = 60;

        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getAudience() { return audience; }
        public void setAudience(String audience) { this.audience = audience; }
        public long getExpirationMinutes() { return expirationMinutes; }
        public void setExpirationMinutes(long expirationMinutes) { this.expirationMinutes = expirationMinutes; }
    }

    public static class Auth {
        private String username = "admin";
        private String password = "admin123";

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}