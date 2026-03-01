package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cognito_sub", unique = true, nullable = false)
    private String cognitoSub;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_CONFIRMATION;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    @Column(name = "mfa_enabled", nullable = false)
    @Builder.Default
    private boolean mfaEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status", nullable = false)
    @Builder.Default
    private OnboardingStatus onboardingStatus = OnboardingStatus.NOT_STARTED;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    @Column(name = "reset_code")
    private String resetCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.cognitoSub == null) {
            this.cognitoSub = UUID.randomUUID().toString();
        }
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
