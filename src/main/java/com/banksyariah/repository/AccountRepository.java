package com.banksyariah.repository;

import com.banksyariah.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByAccountNumber(String accountNumber);
    Page<Account> findByUserId(String userId, Pageable pageable);
    boolean existsByAccountNumber(String accountNumber);
}