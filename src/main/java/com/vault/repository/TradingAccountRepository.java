package com.vault.repository;

import com.vault.entity.TradingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradingAccountRepository extends JpaRepository<TradingAccount, Long> {

    List<TradingAccount> findByUserId(Long userId);

    Optional<TradingAccount> findByAlpacaAccountId(String alpacaAccountId);
}
