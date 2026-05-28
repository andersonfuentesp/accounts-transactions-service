-- DDL para PostgreSQL (perfil productivo).
-- En el perfil por defecto (H2) el esquema se crea automaticamente via JPA.

CREATE TABLE IF NOT EXISTS accounts (
                                        id          UUID PRIMARY KEY,
                                        owner       VARCHAR(255)   NOT NULL,
    currency    VARCHAR(3)     NOT NULL,
    balance     NUMERIC(19,2)  NOT NULL,
    version     BIGINT         NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ    NOT NULL
    );

CREATE TABLE IF NOT EXISTS transactions (
                                            id          UUID PRIMARY KEY,
                                            account_id  UUID           NOT NULL REFERENCES accounts(id),
    type        VARCHAR(10)    NOT NULL,
    amount      NUMERIC(19,2)  NOT NULL,
    currency    VARCHAR(3)     NOT NULL,
    created_at  TIMESTAMPTZ    NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_tx_account_created
    ON transactions (account_id, created_at);

CREATE TABLE IF NOT EXISTS idempotency_keys (
                                                idempotency_key VARCHAR(200) PRIMARY KEY,
    request_hash    VARCHAR(64)  NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    result_ref      VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL
    );