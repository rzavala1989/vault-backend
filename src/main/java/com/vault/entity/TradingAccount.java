package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trading_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradingAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "alpaca_account_id")
    private String alpacaAccountId;

    @Column(name = "account_number")
    private String accountNumber;

    @Builder.Default
    private String status = "ACTIVE";
}
