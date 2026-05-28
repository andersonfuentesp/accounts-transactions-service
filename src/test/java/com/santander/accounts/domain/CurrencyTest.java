package com.santander.accounts.domain;

import com.santander.accounts.domain.exception.UnsupportedCurrencyException;
import com.santander.accounts.domain.model.Currency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyTest {

    @Test
    void acepta_monedas_soportadas_y_normaliza() {
        assertEquals("PEN", Currency.of("pen").code());
        assertEquals("USD", Currency.of(" usd ").code());
    }

    @Test
    void rechaza_moneda_no_soportada() {
        assertThrows(UnsupportedCurrencyException.class, () -> Currency.of("JPY"));
    }

    @Test
    void rechaza_moneda_nula() {
        assertThrows(UnsupportedCurrencyException.class, () -> Currency.of(null));
    }
}