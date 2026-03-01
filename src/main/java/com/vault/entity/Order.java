package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "order_id", unique = true)
    private String orderId;

    @Column(nullable = false)
    private String symbol;

    @Column(precision = 19, scale = 8)
    private BigDecimal qty;

    @Column(nullable = false)
    private String side;

    @Column(name = "order_type", nullable = false)
    private String orderType;

    @Column(name = "time_in_force")
    @Builder.Default
    private String timeInForce = "day";

    @Builder.Default
    private String status = "new";

    @Column(name = "filled_qty", precision = 19, scale = 8)
    @Builder.Default
    private BigDecimal filledQty = BigDecimal.ZERO;

    @Column(name = "filled_avg_price", precision = 19, scale = 4)
    private BigDecimal filledAvgPrice;

    @Column(name = "limit_price", precision = 19, scale = 4)
    private BigDecimal limitPrice;

    @Column(name = "stop_price", precision = 19, scale = 4)
    private BigDecimal stopPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
