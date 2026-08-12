package com.medipass.server.domain.regulation.source;

import com.medipass.server.domain.regulation.web.dto.RegulationRecord;
import com.medipass.server.domain.regulation.entity.PreparationLevel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 일본(JP) 규제약물 Source
 * PDF에서 추출한 TSV 두 개를 읽어 공통 형식으로 변환
 *
 * 1) controlled_substances.tsv : 규제 목록 (물질/분류/금지여부)
 * 2) psychotropic_limits.tsv   : 향정신성 반입 허용 한도 (물질/양/단위)
 * 원천: 후생노동성 Controlled Substances List
 */
@Component
public class JpRegulationSource implements CountryRegulationSource {

    private static final String COUNTRY_CODE = "JP";
    private static final String TSV_PATH = "data/regulations/jp/controlled_substances.tsv";
    private static final String LIMITS_TSV_PATH = "data/regulations/jp/psychotropic_limits.tsv";
    // 공식 근거 문서 URL (후생노동성)
    private static final String SOURCE_URL =
            "https://www.ncd.mhlw.go.jp/dl_data/keitai/cotrolled_substances_list20241212%20.pdf";
    private static final String META_DATE_PREFIX = "# verified_date=";

    @Override
    public String countryCode() {
        return COUNTRY_CODE;
    }

    @Override
    public List<RegulationRecord> load() {
        Map<String, String[]> limits = loadLimits(); // 물질명 → [양, 단위]
        List<RegulationRecord> records = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(TSV_PATH);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            LocalDate verifiedDate = null;
            boolean headerSkipped = false;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                // 메타 줄에서 기준일 추출
                if (line.startsWith("#")) {
                    if (line.startsWith(META_DATE_PREFIX)) {
                        verifiedDate = LocalDate.parse(line.substring(META_DATE_PREFIX.length()).strip());
                    }
                    continue;
                }
                // 첫 데이터가 아닌 줄(컬럼 헤더) 한 번 skip
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                String[] cols = line.split("\t", -1);
                if (cols.length < 3) {
                    continue;
                }
                String name = cols[0].strip();
                String category = cols[1].strip();
                boolean prohibited = "true".equalsIgnoreCase(cols[2].strip());

                // 이름이 한도표에 있으면 mg 으로 환산해 붙임 (없으면 null)
                String[] limit = limits.get(name);
                BigDecimal limitMg = (limit != null) ? toMg(limit[0], limit[1]) : null;

                records.add(new RegulationRecord(
                        name,
                        category,
                        toPreparationLevel(prohibited),
                        limitMg,
                        SOURCE_URL,
                        verifiedDate
                ));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("일본 규제 TSV 로드 실패: " + TSV_PATH, e);
        }

        return records;
    }

    // 향정신성 한도 TSV → 물질명 기준 맵 (양/단위)
    private Map<String, String[]> loadLimits() {
        Map<String, String[]> limits = new HashMap<>();
        ClassPathResource resource = new ClassPathResource(LIMITS_TSV_PATH);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // 헤더 skip
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] cols = line.split("\t", -1);
                if (cols.length < 3) {
                    continue;
                }
                limits.put(cols[0].strip(), new String[]{cols[1].strip(), cols[2].strip()});
            }
        } catch (IOException e) {
            throw new UncheckedIOException("일본 향정신성 한도 TSV 로드 실패: " + LIMITS_TSV_PATH, e);
        }
        return limits;
    }

    // 수출입 금지면 반입 불가(NOT_ALLOWED), 그 외 목록 등재분은 준비 필요(PREP_REQUIRED)
    private PreparationLevel toPreparationLevel(boolean prohibited) {
        return prohibited ? PreparationLevel.NOT_ALLOWED : PreparationLevel.PREP_REQUIRED;
    }

    // 한도를 mg 로 통일 (G → ×1000, MG → 그대로)
    private BigDecimal toMg(String amount, String unit) {
        BigDecimal value = new BigDecimal(amount);
        return "G".equalsIgnoreCase(unit) ? value.multiply(BigDecimal.valueOf(1000)) : value;
    }
}
