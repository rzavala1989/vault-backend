package com.vault.repository;

import com.vault.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByIso3(String iso3);

    Optional<Country> findByIso2(String iso2);
}
