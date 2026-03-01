package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account_memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Builder.Default
    private String role = "OWNER";

    @Builder.Default
    private String status = "ACTIVE";
}
