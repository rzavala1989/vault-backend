package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String symbol;

    private String name;

    private String exchange;

    @Builder.Default
    private boolean tradable = true;

    @Builder.Default
    private boolean fractionable = false;

    @Builder.Default
    private String status = "active";

    @Column(name = "asset_class")
    @Builder.Default
    private String assetClass = "us_equity";
}
