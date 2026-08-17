package com.medipass.server.domain.emergency.repository;

import com.medipass.server.domain.emergency.entity.OverseasMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OverseasMissionRepository extends JpaRepository<OverseasMission, Long> {

    // 최근접 공관 계산용 — 국가당 10건 안팎이라 전부 읽어 메모리에서 고른다
    List<OverseasMission> findByCountry_Code(String countryCode);

    // 동기화 여부 판단용 — 국가 단위로 봐야 나중에 국가를 추가할 때 건너뛰지 않는다
    long countByCountry_Code(String countryCode);
}
