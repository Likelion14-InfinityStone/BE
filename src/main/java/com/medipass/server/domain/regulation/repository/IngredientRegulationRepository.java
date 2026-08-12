package com.medipass.server.domain.regulation.repository;

import com.medipass.server.domain.regulation.entity.IngredientRegulation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRegulationRepository extends JpaRepository<IngredientRegulation, Long> {

    List<IngredientRegulation> findByCountry_Code(String countryCode);

    // 판정용: 국가 + 성분명으로 규제 1건 조회
    Optional<IngredientRegulation> findByCountry_CodeAndSubstanceName(String countryCode, String substanceName);
}
