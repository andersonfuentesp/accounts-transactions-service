package com.santander.accounts.infrastructure.adapter.out.persistence;

import com.santander.accounts.application.port.out.IdempotencyRecord;
import com.santander.accounts.application.port.out.IdempotencyStore;
import com.santander.accounts.infrastructure.adapter.out.persistence.entity.IdempotencyKeyJpaEntity;
import com.santander.accounts.infrastructure.adapter.out.persistence.repository.IdempotencyKeyJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class IdempotencyStoreAdapter implements IdempotencyStore {

    private final IdempotencyKeyJpaRepository jpa;

    public IdempotencyStoreAdapter(IdempotencyKeyJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<IdempotencyRecord> tryBegin(String key, String requestHash) {
        Optional<IdempotencyKeyJpaEntity> current = jpa.findById(key);
        if (current.isPresent()) {
            return current.map(this::toRecord);
        }
        try {
            jpa.saveAndFlush(new IdempotencyKeyJpaEntity(
                    key, requestHash, IdempotencyRecord.IN_PROGRESS, null, Instant.now()));
            return Optional.empty();
        } catch (DataIntegrityViolationException raceCondition) {
            // Otra petición concurrente insertó la misma clave primero
            return jpa.findById(key).map(this::toRecord);
        }
    }

    @Override
    public void complete(String key, String resultRef) {
        jpa.findById(key).ifPresent(e -> {
            e.setStatus(IdempotencyRecord.COMPLETED);
            e.setResultRef(resultRef);
            jpa.save(e);
        });
    }

    @Override
    public void remove(String key) {
        jpa.deleteById(key);
    }

    private IdempotencyRecord toRecord(IdempotencyKeyJpaEntity e) {
        return new IdempotencyRecord(e.getKey(), e.getRequestHash(), e.getStatus(), e.getResultRef());
    }
}