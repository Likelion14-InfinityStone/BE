package com.medipass.server.domain.country.repository;

import com.medipass.server.domain.country.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, String> {

    // 3자리 코드(KOR)로 국가 조회
    Optional<Country> findByCodeAlpha3(String codeAlpha3);
}
