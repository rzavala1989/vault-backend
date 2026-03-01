package com.vault.repository;

import com.vault.entity.UserFinancial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFinancialRepository extends JpaRepository<UserFinancial, Long> {

    Optional<UserFinancial> findByUserId(Long userId);
}
