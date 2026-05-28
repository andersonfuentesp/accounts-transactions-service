package com.santander.accounts.infrastructure.adapter.out.persistence;

import com.santander.accounts.application.port.out.AccountRepository;
import com.santander.accounts.domain.model.Account;
import com.santander.accounts.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.santander.accounts.infrastructure.adapter.out.persistence.repository.AccountJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpa;

    public AccountRepositoryAdapter(AccountJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = PersistenceMapper.toEntity(account);
        if (jpa.existsById(account.id())) {
            entity.markNotNew(); // fuerza UPDATE -> activa el chequeo de @Version
        }
        AccountJpaEntity saved = jpa.save(entity);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Account> findAll(int offset, int limit) {
        int page = limit > 0 ? offset / limit : 0;
        return jpa.findAll(PageRequest.of(page, Math.max(limit, 1),
                        Sort.by(Sort.Direction.ASC, "createdAt")))
                .map(PersistenceMapper::toDomain)
                .getContent();
    }

    @Override
    public long count() {
        return jpa.count();
    }
}