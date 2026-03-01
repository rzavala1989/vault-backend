package com.vault.repository;

import com.vault.entity.AccountMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountMembershipRepository extends JpaRepository<AccountMembership, Long> {

    List<AccountMembership> findByUserId(Long userId);

    List<AccountMembership> findByAccountId(Long accountId);
}
