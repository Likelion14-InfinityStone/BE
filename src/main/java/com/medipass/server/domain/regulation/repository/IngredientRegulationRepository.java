package com.medipass.server.domain.regulation.repository;

import com.medipass.server.domain.regulation.entity.IngredientRegulation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRegulationRepository extends JpaRepository<IngredientRegulation, Long> {

    List<IngredientRegulation> findByCountry_Code(String countryCode);
}
