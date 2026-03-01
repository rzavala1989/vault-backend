package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "countries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 3)
    private String iso3;

    @Column(nullable = false, length = 2)
    private String iso2;

    @Column(nullable = false)
    private String name;

    @Column(name = "flag_emoji")
    private String flagEmoji;
}
