package com.medipass.server.global.init;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.domain.country.repository.CountryRepository;
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

    // 국가 마스터 시드 (code, name_ko, name_en, local_lang) — 10개국
    private static final List<String[]> COUNTRIES = List.of(
            new String[]{"AU", "호주", "Australia", "en"},
            new String[]{"CN", "중국", "China", "zh"},
            new String[]{"DE", "독일", "Germany", "de"},
            new String[]{"FR", "프랑스", "France", "fr"},
            new String[]{"GB", "영국", "United Kingdom", "en"},
            new String[]{"JP", "일본", "Japan", "ja"},
            new String[]{"KR", "대한민국", "South Korea", "ko"},
            new String[]{"TH", "태국", "Thailand", "th"},
            new String[]{"US", "미국", "United States", "en"},
            new String[]{"VN", "베트남", "Vietnam", "vi"}
    );

    @Override
    public void run(ApplicationArguments args) {
        seedCountries();
        regulationImportService.importAll();
        requirementImportService.importAll();
    }

    private void seedCountries() {
        for (String[] c : COUNTRIES) {
            if (!countryRepository.existsById(c[0])) {
                countryRepository.save(Country.builder()
                        .code(c[0])
                        .nameKo(c[1])
                        .nameEn(c[2])
                        .localLang(c[3])
                        .build());
            }
        }
        log.info("[국가시드] {}개국 확인/생성 완료", COUNTRIES.size());
    }
}
