package com.santander.accounts.domain;

import com.santander.accounts.domain.exception.CurrencyMismatchException;
import com.santander.accounts.domain.model.Currency;
import com.santander.accounts.domain.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    private final Currency PEN = Currency.of("PEN");
    private final Currency USD = Currency.of("USD");

    @Test
    void suma_y_resta_en_la_misma_moneda() {
        Money a = Money.of(new BigDecimal("100.00"), PEN);
        Money b = Money.of(new BigDecimal("30.50"), PEN);
        assertEquals(new BigDecimal("130.50"), a.add(b).amount());
        assertEquals(new BigDecimal("69.50"), a.subtract(b).amount());
    }

    @Test
    void operar_entre_monedas_distintas_lanza_excepcion() {
        Money pen = Money.of(new BigDecimal("100.00"), PEN);
        Money usd = Money.of(new BigDecimal("10.00"), USD);
        assertThrows(CurrencyMismatchException.class, () -> pen.add(usd));
    }

    @Test
    void detecta_negativos_y_comparaciones() {
        Money cero = Money.of(new BigDecimal("0.00"), PEN);
        Money diez = Money.of(new BigDecimal("10.00"), PEN);
        assertFalse(cero.isNegative());
        assertTrue(cero.isLessThan(diez));
        assertFalse(diez.isLessThan(cero));
    }
}