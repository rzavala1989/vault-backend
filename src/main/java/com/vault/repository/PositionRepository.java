package com.vault.repository;

import com.vault.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByAccountId(String accountId);

    Optional<Position> findByAccountIdAndSymbol(String accountId, String symbol);
}
