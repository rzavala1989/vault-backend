package com.vault.repository;

import com.vault.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {

    List<UserWallet> findByUserId(Long userId);

    Optional<UserWallet> findByUserIdAndPrimaryTrue(Long userId);
}
