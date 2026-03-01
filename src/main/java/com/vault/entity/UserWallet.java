package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String address;

    private String chain;

    @Column(name = "is_primary")
    @Builder.Default
    private boolean primary = false;
}
