package com.vault.repository;

import com.vault.entity.UserTaxForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTaxFormRepository extends JpaRepository<UserTaxForm, Long> {

    List<UserTaxForm> findByUserId(Long userId);
}
