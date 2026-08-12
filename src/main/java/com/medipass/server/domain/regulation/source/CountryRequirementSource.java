package com.medipass.server.domain.regulation.source;

import com.medipass.server.domain.regulation.web.dto.RequirementTemplateRecord;

import java.util.List;

/**
 * 나라별 서류 템플릿을 제공하는 공통 규칙(인터페이스)
 * 새 나라 추가 = 이 인터페이스를 구현한 클래스 하나 추가
 */
public interface CountryRequirementSource {

    // 담당 국가 코드 (ISO 3166-1 alpha-2, 예: JP)
    String countryCode();

    // 이 나라의 분류별 서류 템플릿 목록
    List<RequirementTemplateRecord> load();
}
