package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(nullable = false)
    private String symbol;

    @Column(precision = 19, scale = 8)
    private BigDecimal qty;

    @Column(name = "market_value", precision = 19, scale = 4)
    private BigDecimal marketValue;

    @Column(name = "unrealized_pl", precision = 19, scale = 4)
    private BigDecimal unrealizedPl;

    @Column(name = "cost_basis", precision = 19, scale = 4)
    private BigDecimal costBasis;

    @Column(name = "current_price", precision = 19, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "avg_entry_price", precision = 19, scale = 4)
    private BigDecimal avgEntryPrice;

    private Instant timestamp;
}
