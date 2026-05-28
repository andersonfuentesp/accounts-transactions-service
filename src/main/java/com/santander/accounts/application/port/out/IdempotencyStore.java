package com.santander.accounts.application.port.out;

import java.util.Optional;

public interface IdempotencyStore {

    /**
     * Intenta registrar la clave con estado IN_PROGRESS de forma atómica.
     * @return Optional.empty() si la clave es nueva (se debe procesar),
     *         o el registro existente si ya estaba (para decidir replay o conflicto).
     */
    Optional<IdempotencyRecord> tryBegin(String key, String requestHash);

    /** Marca la clave como COMPLETED y guarda la referencia al resultado. */
    void complete(String key, String resultRef);

    /** Libera la clave (cuando el procesamiento falló y debe poder reintentarse). */
    void remove(String key);
}