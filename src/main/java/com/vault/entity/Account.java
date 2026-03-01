package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Builder.Default
    private String currency = "USD";

    @Column(name = "buying_power", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal buyingPower = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal cash = BigDecimal.ZERO;

    @Column(name = "portfolio_value", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal portfolioValue = BigDecimal.ZERO;

    @Column(name = "enabled_assets")
    private String enabledAssets;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
