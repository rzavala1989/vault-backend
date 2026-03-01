package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_identity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "id_number_encrypted")
    private String idNumberEncrypted;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "verification_status")
    @Builder.Default
    private String verificationStatus = "not_started";
}
