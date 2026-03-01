package com.vault.repository;

import com.vault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByCognitoSub(String cognitoSub);

    boolean existsByEmail(String email);

    Optional<User> findByConfirmationCode(String code);

    Optional<User> findByResetCode(String code);
}
