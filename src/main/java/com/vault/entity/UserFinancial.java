package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_financial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFinancial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "annual_income_min")
    private String annualIncomeMin;

    @Column(name = "annual_income_max")
    private String annualIncomeMax;

    @Column(name = "liquid_net_worth_min")
    private String liquidNetWorthMin;

    @Column(name = "liquid_net_worth_max")
    private String liquidNetWorthMax;

    @Column(name = "total_net_worth_min")
    private String totalNetWorthMin;

    @Column(name = "total_net_worth_max")
    private String totalNetWorthMax;

    @Column(name = "funding_sources")
    private String fundingSources;
}
