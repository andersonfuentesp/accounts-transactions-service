package com.santander.accounts.infrastructure.adapter.out.persistence.repository;

import com.santander.accounts.infrastructure.adapter.out.persistence.entity.IdempotencyKeyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyJpaEntity, String> {
}