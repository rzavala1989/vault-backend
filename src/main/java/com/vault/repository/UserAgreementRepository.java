package com.vault.repository;

import com.vault.entity.UserAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {

    List<UserAgreement> findByUserId(Long userId);
}
