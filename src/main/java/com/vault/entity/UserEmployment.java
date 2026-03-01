package com.vault.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_employment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEmployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "employment_status")
    private String employmentStatus;

    private String employer;

    private String position;

    @Column(name = "employer_address")
    private String employerAddress;
}
