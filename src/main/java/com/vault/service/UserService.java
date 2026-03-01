package com.vault.service;

import com.vault.dto.UserResponse;
import com.vault.entity.User;
import com.vault.entity.UserProfile;
import com.vault.entity.UserRole;
import com.vault.entity.UserStatus;
import com.vault.exception.ApiException;
import com.vault.repository.UserProfileRepository;
import com.vault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));
    }

    public UserResponse toResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .cognitoSub(user.getCognitoSub())
                .mfaEnabled(user.isMfaEnabled())
                .onboardingStatus(user.getOnboardingStatus().name())
                .createdAt(user.getCreatedAt().toString());

        userProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            builder.firstName(profile.getFirstName());
            builder.lastName(profile.getLastName());
            builder.phone(profile.getPhone());
        });

        return builder.build();
    }

    public List<UserResponse> listAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void disableUser(Long id) {
        User user = getById(id);
        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(Long id) {
        User user = getById(id);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void updateRole(Long id, String role) {
        User user = getById(id);
        try {
            user.setRole(UserRole.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("INVALID_ROLE", "Invalid role: " + role);
        }
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = getById(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getById(id);
        userRepository.delete(user);
    }

    @Transactional
    public void grantApiAccess(Long id) {
        User user = getById(id);
        user.setRole(UserRole.API_USER);
        userRepository.save(user);
    }

    @Transactional
    public void revokeApiAccess(Long id) {
        User user = getById(id);
        if (user.getRole() == UserRole.API_USER) {
            user.setRole(UserRole.USER);
            userRepository.save(user);
        }
    }
}
