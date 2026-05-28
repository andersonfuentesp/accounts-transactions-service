package com.santander.accounts.infrastructure.adapter.out.persistence;

import com.santander.accounts.application.port.out.TransactionRepository;
import com.santander.accounts.domain.model.Transaction;
import com.santander.accounts.domain.model.TransactionType;
import com.santander.accounts.infrastructure.adapter.out.persistence.repository.TransactionJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpa;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return PersistenceMapper.toDomain(jpa.save(PersistenceMapper.toEntity(transaction)));
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Transaction> findByAccount(UUID accountId, Instant from, Instant to,
                                           TransactionType type, int offset, int limit) {
        int page = limit > 0 ? offset / limit : 0;
        return jpa.search(accountId, from, to, type,
                        PageRequest.of(page, Math.max(limit, 1),
                                Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(PersistenceMapper::toDomain)
                .getContent();
    }

    @Override
    public long countByAccount(UUID accountId, Instant from, Instant to, TransactionType type) {
        return jpa.countSearch(accountId, from, to, type);
    }
}