package com.santander.accounts.infrastructure.security;

import com.santander.accounts.application.port.out.TokenProvider;
import com.santander.accounts.infrastructure.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final AppProperties props;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtTokenProvider(AppProperties props) {
        this.props = props;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el par de claves RSA", e);
        }
    }

    @Override
    public String issueToken(String subject) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getJwt().getExpirationMinutes() * 60);
        return Jwts.builder()
                .subject(subject)
                .issuer(props.getJwt().getIssuer())
                .audience().add(props.getJwt().getAudience()).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /** Valida firma, emisor, expiración y audiencia. Devuelve el subject. */
    public String validateAndGetSubject(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(props.getJwt().getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!claims.getAudience().contains(props.getJwt().getAudience())) {
            throw new io.jsonwebtoken.JwtException("Audiencia inválida");
        }
        return claims.getSubject();
    }
}