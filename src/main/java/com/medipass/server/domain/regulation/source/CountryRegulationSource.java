package com.medipass.server.domain.regulation.source;

import com.medipass.server.domain.regulation.web.dto.RegulationRecord;

import java.util.List;

/**
 * 나라별 규제 데이터를 읽어오는 공통 규칙(인터페이스)
 * 새 나라 추가 = 이 인터페이스를 구현한 클래스 하나 추가
 */
public interface CountryRegulationSource {

    // 담당 국가 코드 (ISO 3166-1 alpha-2, 예: KR)
    String countryCode();

    // 이 나라 규제 목록을 공통 형식으로 변환해 반환
    List<RegulationRecord> load();
}
