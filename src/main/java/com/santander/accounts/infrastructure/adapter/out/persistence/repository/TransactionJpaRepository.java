package com.santander.accounts.infrastructure.adapter.out.persistence.repository;

import com.santander.accounts.domain.model.TransactionType;
import com.santander.accounts.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    @Query("""
            select t from TransactionJpaEntity t
            where t.accountId = :accountId
              and (:type is null or t.type = :type)
              and (:from is null or t.createdAt >= :from)
              and (:to   is null or t.createdAt <= :to)
            """)
    Page<TransactionJpaEntity> search(@Param("accountId") UUID accountId,
                                      @Param("from") Instant from,
                                      @Param("to") Instant to,
                                      @Param("type") TransactionType type,
                                      Pageable pageable);

    @Query("""
            select count(t) from TransactionJpaEntity t
            where t.accountId = :accountId
              and (:type is null or t.type = :type)
              and (:from is null or t.createdAt >= :from)
              and (:to   is null or t.createdAt <= :to)
            """)
    long countSearch(@Param("accountId") UUID accountId,
                     @Param("from") Instant from,
                     @Param("to") Instant to,
                     @Param("type") TransactionType type);
}