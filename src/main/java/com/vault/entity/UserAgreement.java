package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "user_agreements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "agreement_type", nullable = false)
    private String agreementType;

    @Column(name = "signed_at", nullable = false)
    @Builder.Default
    private Instant signedAt = Instant.now();

    @Column(name = "ip_address")
    private String ipAddress;

    private String revision;
}
