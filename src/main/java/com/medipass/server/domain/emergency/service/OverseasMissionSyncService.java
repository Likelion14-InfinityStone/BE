package com.medipass.server.domain.emergency.service;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.domain.country.repository.CountryRepository;
import com.medipass.server.domain.emergency.entity.OverseasMission;
import com.medipass.server.domain.emergency.repository.OverseasMissionRepository;
import com.medipass.server.global.mofa.client.MofaClient;
import com.medipass.server.global.mofa.dto.MofaMissionItem;
import com.medipass.server.global.mofa.util.PhoneNumberNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 재외공관 동기화 — 외교부 API 응답을 우리 DB 로 옮긴다
 *
 * 앱 시작 시 국가별로 한 번만 가져오고, 이후 조회는 우리 DB 만 본다.
 * 응급 화면이 외부 API 지연·장애에 물리면 안 되기 때문이다.
 * 실패해도 예외를 밖으로 던지지 않는다 — 앱은 떠야 하고, 저장이 안 됐으면
 * 다음 기동 때 여전히 비어 있으므로 자동으로 재시도된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OverseasMissionSyncService {

    private final MofaClient mofaClient;
    private final OverseasMissionRepository missionRepository;
    private final CountryRepository countryRepository;

    // 국가 마스터의 모든 국가를 대상으로 (이미 받아둔 국가는 건너뜀)
    public void syncAllIfAbsent() {
        countryRepository.findAll().forEach(this::syncIfAbsent);
    }

    /**
     * 해당 국가 공관이 하나도 없을 때만 가져온다.
     * 판단을 국가 단위로 하는 이유는, 전체 테이블이 비었는지로 보면
     * 나중에 국가를 추가할 때 기존 국가 데이터가 있다고 건너뛰기 때문이다.
     */
    public void syncIfAbsent(Country country) {
        String code = country.getCode();
        if (missionRepository.countByCountry_Code(code) > 0) {
            return;
        }

        try {
            List<MofaMissionItem> items = mofaClient.findMissionsByCountry(code);
            List<OverseasMission> missions = items.stream()
                    .filter(item -> item.embassyCd() != null && !item.embassyCd().isBlank()) // UNIQUE 키
                    .map(item -> toEntity(country, item))
                    .toList();

            if (missions.isEmpty()) {
                log.info("[재외공관] {} 응답에 공관이 없음 — 다음 기동 때 다시 시도", code);
                return;
            }

            // 한 번에 저장해 부분 저장을 막는다.
            // 10건 중 3건만 들어가면 다음 기동 때 '있음'으로 판단해 나머지가 영영 안 들어온다.
            missionRepository.saveAll(missions);
            log.info("[재외공관] {} {}건 적재 완료", code, missions.size());

        } catch (Exception e) {
            log.warn("[재외공관] {} 적재 실패 — 다음 기동 때 재시도: {}", code, e.getMessage());
        }
    }

    private OverseasMission toEntity(Country country, MofaMissionItem item) {
        String urgencyTelNo = PhoneNumberNormalizer.toE164(item.urgencyTelNo());

        return OverseasMission.builder()
                .embassyCd(item.embassyCd())
                .country(country)
                .nameKo(item.nameKo())
                .missionTypeCode(item.missionTypeCode())
                .missionTypeName(item.missionTypeName())
                .telNo(PhoneNumberNormalizer.toE164(item.telNo()))
                .urgencyTelNo(urgencyTelNo)
                // 국가별 국가번호 표가 없어서 '한국 번호가 아니면 현지' 로 근사한다.
                // 재외공관은 한국 밖에만 있으므로 지금 데이터에서는 이걸로 충분하다.
                .urgencyLocal(urgencyTelNo != null && !PhoneNumberNormalizer.isKoreanNumber(urgencyTelNo))
                .address(item.address())
                .latitude(item.latitude())
                .longitude(item.longitude())
                .build();
    }
}
