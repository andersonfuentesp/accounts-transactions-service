package com.santander.accounts.domain;

import com.santander.accounts.domain.exception.CurrencyMismatchException;
import com.santander.accounts.domain.exception.InsufficientFundsException;
import com.santander.accounts.domain.exception.InvalidAccountDataException;
import com.santander.accounts.domain.model.Account;
import com.santander.accounts.domain.model.Currency;
import com.santander.accounts.domain.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private final Currency PEN = Currency.of("PEN");

    private Account cuentaCon(String saldo) {
        return Account.open("Anderson", PEN, Money.of(new BigDecimal(saldo), PEN));
    }

    @Test
    void crea_cuenta_valida() {
        Account a = cuentaCon("100.00");
        assertEquals("Anderson", a.owner());
        assertEquals(new BigDecimal("100.00"), a.balance().amount());
    }

    @Test
    void rechaza_titular_vacio() {
        assertThrows(InvalidAccountDataException.class,
                () -> Account.open("  ", PEN, Money.of(new BigDecimal("10.00"), PEN)));
    }

    @Test
    void rechaza_saldo_inicial_negativo() {
        assertThrows(InvalidAccountDataException.class,
                () -> Account.open("Anderson", PEN, Money.of(new BigDecimal("-1.00"), PEN)));
    }

    @Test
    void debito_descuenta_saldo() {
        Account a = cuentaCon("100.00");
        a.debit(Money.of(new BigDecimal("30.00"), PEN));
        assertEquals(new BigDecimal("70.00"), a.balance().amount());
    }

    @Test
    void debito_sin_saldo_suficiente_lanza_excepcion() {
        Account a = cuentaCon("20.00");
        assertThrows(InsufficientFundsException.class,
                () -> a.debit(Money.of(new BigDecimal("50.00"), PEN)));
    }

    @Test
    void credito_aumenta_saldo() {
        Account a = cuentaCon("100.00");
        a.credit(Money.of(new BigDecimal("25.00"), PEN));
        assertEquals(new BigDecimal("125.00"), a.balance().amount());
    }

    @Test
    void transaccion_con_otra_moneda_lanza_excepcion() {
        Account a = cuentaCon("100.00");
        assertThrows(CurrencyMismatchException.class,
                () -> a.debit(Money.of(new BigDecimal("10.00"), Currency.of("USD"))));
    }
}