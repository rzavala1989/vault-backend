package com.vault.repository;

import com.vault.entity.UserEmployment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEmploymentRepository extends JpaRepository<UserEmployment, Long> {

    Optional<UserEmployment> findByUserId(Long userId);
}
