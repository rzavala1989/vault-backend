package com.vault.repository;

import com.vault.entity.Firm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FirmRepository extends JpaRepository<Firm, Long> {

    Optional<Firm> findByOwnerId(Long userId);
}
