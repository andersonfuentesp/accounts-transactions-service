package com.santander.accounts.application.port.out;

/** Registro de una clave de idempotencia. resultRef apunta al id de la transacción creada. */
public record IdempotencyRecord(String key, String requestHash, String status, String resultRef) {
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";
}