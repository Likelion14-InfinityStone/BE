package com.medipass.server.domain.regulation.service;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.domain.country.repository.CountryRepository;
import com.medipass.server.domain.regulation.web.dto.RegulationRecord;
import com.medipass.server.domain.regulation.entity.IngredientRegulation;
import com.medipass.server.domain.regulation.repository.IngredientRegulationRepository;
import com.medipass.server.domain.regulation.source.CountryRegulationSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 규제 데이터 적재/최신화 서비스
 * 국가별 Source가 내놓은 레코드를 (country_code, inn) 기준으로 UPSERT
 * - 기존 행: id 유지한 채 갱신 (DELETE+INSERT 안 씀 → 서류 연결 유지)
 * - 신규 행: INSERT
 * - 최신 목록에서 빠진 행: 삭제 대신 비활성(active=false)
 */
@Slf4j
@Service
public class RegulationImportService {

    private final IngredientRegulationRepository regulationRepository;
    private final CountryRepository countryRepository;
    private final Map<String, CountryRegulationSource> sourcesByCountry;

    public RegulationImportService(
            IngredientRegulationRepository regulationRepository,
            CountryRepository countryRepository,
            List<CountryRegulationSource> sources
    ) {
        this.regulationRepository = regulationRepository;
        this.countryRepository = countryRepository;
        this.sourcesByCountry = sources.stream()
                .collect(Collectors.toMap(
                        s -> s.countryCode().toUpperCase(),
                        Function.identity()));
    }

    // 등록된 모든 국가 Source 적재 (앱 시작 시 자동 실행용)
    public void importAll() {
        sourcesByCountry.keySet().forEach(this::importCountry);
    }

    // 특정 국가 적재/최신화
    @Transactional
    public void importCountry(String countryCode) {
        String code = countryCode.toUpperCase();
        CountryRegulationSource source = sourcesByCountry.get(code);
        if (source == null) {
            throw new IllegalArgumentException("지원하지 않는 국가 코드입니다: " + countryCode);
        }

        Country country = countryRepository.findById(code)
                .orElseThrow(() -> new IllegalStateException(
                        "국가 마스터에 없는 코드입니다: " + code + " — DataInitializer 의 COUNTRIES 에 추가하세요"));

        List<RegulationRecord> records = source.load();

        // 기존 행을 원문 물질명으로 인덱싱
        Map<String, IngredientRegulation> existingByName = new HashMap<>();
        for (IngredientRegulation e : regulationRepository.findByCountry_Code(code)) {
            existingByName.put(e.getSubstanceName(), e);
        }

        int inserted = 0, updated = 0;
        Set<String> incomingNames = new HashSet<>();

        for (RegulationRecord r : records) {
            incomingNames.add(r.substanceName());
            IngredientRegulation existing = existingByName.get(r.substanceName());
            if (existing != null) {
                existing.applyUpdate(r.categoryCode(), r.preparationLevel(),
                        r.limitMg(), r.sourceUrl(), r.verifiedDate());
                updated++;
            } else {
                regulationRepository.save(IngredientRegulation.builder()
                        .country(country)
                        .substanceName(r.substanceName())
                        .categoryCode(r.categoryCode())
                        .preparationLevel(r.preparationLevel())
                        .limitMg(r.limitMg())
                        .sourceUrl(r.sourceUrl())
                        .verifiedDate(r.verifiedDate())
                        .active(true)
                        .build());
                inserted++;
            }
        }

        // 최신 목록에서 빠진 활성 행은 비활성 처리
        int deactivated = 0;
        for (IngredientRegulation e : existingByName.values()) {
            if (e.isActive() && !incomingNames.contains(e.getSubstanceName())) {
                e.deactivate();
                deactivated++;
            }
        }

        log.info("[규제적재] {} → 신규 {}, 수정 {}, 비활성 {} (원천 {}건)",
                code, inserted, updated, deactivated, records.size());
    }
}
