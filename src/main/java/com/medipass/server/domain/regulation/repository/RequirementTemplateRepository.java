package com.medipass.server.domain.regulation.repository;

import com.medipass.server.domain.regulation.entity.RequirementTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequirementTemplateRepository extends JpaRepository<RequirementTemplate, Long> {

    List<RequirementTemplate> findByCountry_Code(String countryCode);
}
