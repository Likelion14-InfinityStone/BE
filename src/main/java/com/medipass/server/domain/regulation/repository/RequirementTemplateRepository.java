package com.medipass.server.domain.regulation.repository;

import com.medipass.server.domain.regulation.entity.RequirementTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequirementTemplateRepository extends JpaRepository<RequirementTemplate, Long> {

    List<RequirementTemplate> findByCountry_Code(String countryCode);

    // 판정용: 국가 + 분류로 필요 서류 목록 조회
    List<RequirementTemplate> findByCountry_CodeAndCategoryCode(String countryCode, String categoryCode);
}
