package com.santander.accounts.domain.model;

import com.santander.accounts.domain.exception.UnsupportedCurrencyException;
import java.util.Set;

public final class Currency {

    private static final Set<String> SUPPORTED = Set.of("PEN", "USD", "EUR");

    private final String code;

    private Currency(String code) {
        this.code = code;
    }

    public static Currency of(String code) {
        if (code == null) {
            throw new UnsupportedCurrencyException("La moneda es obligatoria");
        }
        String normalized = code.trim().toUpperCase();
        if (!SUPPORTED.contains(normalized)) {
            throw new UnsupportedCurrencyException("Moneda no soportada: " + code);
        }
        return new Currency(normalized);
    }

    public String code() { return code; }

    public static Set<String> supported() { return SUPPORTED; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency other)) return false;
        return code.equals(other.code);
    }

    @Override
    public int hashCode() { return code.hashCode(); }

    @Override
    public String toString() { return code; }
}