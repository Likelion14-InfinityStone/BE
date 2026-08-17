package com.medipass.server.global.init;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.domain.country.repository.CountryRepository;
import com.medipass.server.domain.emergency.service.OverseasMissionSyncService;
import com.medipass.server.domain.regulation.service.RegulationImportService;
import com.medipass.server.domain.regulation.service.RequirementImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 앱 시작 시 초기 데이터 적재.
 * 국가 마스터를 먼저 시드한 뒤(규제의 country_code FK 대상), 규제 데이터를 적재한다.
 * 규제 적재는 (country_code, inn) UPSERT 라 재기동해도 안전하게 최신화된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CountryRepository countryRepository;
    private final RegulationImportService regulationImportService;
    private final RequirementImportService requirementImportService;
    private final OverseasMissionSyncService overseasMissionSyncService;

    // 국가 마스터 시드 (code, code_alpha3, name_ko, name_en, local_lang) — 10개국
    private static final List<String[]> COUNTRIES = List.of(
            new String[]{"AU", "AUS", "호주", "Australia", "en"},
            new String[]{"CN", "CHN", "중국", "China", "zh"},
            new String[]{"DE", "DEU", "독일", "Germany", "de"},
            new String[]{"FR", "FRA", "프랑스", "France", "fr"},
            new String[]{"GB", "GBR", "영국", "United Kingdom", "en"},
            new String[]{"JP", "JPN", "일본", "Japan", "ja"},
            new String[]{"KR", "KOR", "대한민국", "South Korea", "ko"},
            new String[]{"TH", "THA", "태국", "Thailand", "th"},
            new String[]{"US", "USA", "미국", "United States", "en"},
            new String[]{"VN", "VNM", "베트남", "Vietnam", "vi"}
    );

    @Override
    public void run(ApplicationArguments args) {
        seedCountries();
        regulationImportService.importAll();
        requirementImportService.importAll();
        // 재외공관은 외부 API 라 매번 부르지 않고, 아직 없는 국가만 한 번 가져온다
        overseasMissionSyncService.syncAllIfAbsent();
    }

    private void seedCountries() {
        // 매 기동마다 upsert — 컬럼 추가 시 기존 행에도 값이 채워지도록
        for (String[] c : COUNTRIES) {
            countryRepository.save(Country.builder()
                    .code(c[0])
                    .codeAlpha3(c[1])
                    .nameKo(c[2])
                    .nameEn(c[3])
                    .localLang(c[4])
                    .build());
        }
        log.info("[국가시드] {}개국 확인/생성 완료", COUNTRIES.size());
    }
}
