package com.vault.repository;

import com.vault.entity.UserApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserApiKeyRepository extends JpaRepository<UserApiKey, Long> {

    List<UserApiKey> findByUserId(Long userId);

    Optional<UserApiKey> findByApiKey(String apiKey);
}
