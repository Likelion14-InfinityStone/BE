package com.medipass.server.domain.sos.service;

import com.medipass.server.domain.emergency.entity.OverseasMission;
import com.medipass.server.domain.emergency.repository.OverseasMissionRepository;
import com.medipass.server.domain.sos.entity.SosContactType;
import com.medipass.server.domain.sos.entity.SosSituation;
import com.medipass.server.domain.sos.util.GeoDistance;
import com.medipass.server.domain.sos.web.dto.GeoPoint;
import com.medipass.server.domain.sos.web.dto.SosContact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 결과 화면 연락처 조립 — 현지 긴급번호 / 재외공관 / 의료기관 안내센터 순
 *
 * 채울 수 없는 줄은 빼고 내려준다. 재외공관 동기화가 실패했거나 아직 지원하지 않는
 * 국가면 그 줄이 없을 수 있으므로, 프론트는 개수를 고정으로 보지 말아야 한다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SosContactAssembler {

    // 현지 긴급번호는 공개 API 가 없어 상수로 관리한다. 지금은 일본만 지원한다
    private static final String SUPPORTED_COUNTRY = "JP";
    private static final String JP_POLICE_NAME = "현지 경찰서";
    private static final String JP_POLICE_TEL = "110";
    private static final String JP_AMBULANCE_NAME = "현지 응급 번호";
    private static final String JP_AMBULANCE_TEL = "119";
    private static final String JP_MEDICAL_INFO_NAME = "도쿄도보건의료정보센터";
    private static final String JP_MEDICAL_INFO_TEL = "+81352858181"; // 원문 03-5285-8181 (국내표기)

    // 대사관 유형코드 — 현위치를 모를 때 이 공관을 기본값으로 쓴다
    private static final String MISSION_TYPE_EMBASSY = "10";

    private static final String NOTE_INTERNATIONAL_CALL =
            "현지 번호가 아니라 서울 영사콜센터로 연결됩니다. 국제전화 요금이 발생합니다.";

    private final OverseasMissionRepository missionRepository;

    public List<SosContact> assemble(SosSituation situation, String countryCode, GeoPoint location) {
        List<SosContact> contacts = new ArrayList<>();

        if (SUPPORTED_COUNTRY.equals(countryCode)) {
            contacts.add(localEmergency(situation, contacts.size() + 1));
        }
        findNearestMission(countryCode, location)
                .map(mission -> toContact(mission, contacts.size() + 1))
                .ifPresent(contacts::add);
        if (SUPPORTED_COUNTRY.equals(countryCode)) {
            contacts.add(new SosContact(contacts.size() + 1, SosContactType.MEDICAL_INFO,
                    JP_MEDICAL_INFO_NAME, JP_MEDICAL_INFO_TEL, null));
        }

        return List.copyOf(contacts);
    }

    // 상황에 맞는 현지 신고번호 — 약 부족만 응급번호, 나머지는 경찰
    private SosContact localEmergency(SosSituation situation, int order) {
        return situation.isUsesAmbulance()
                ? new SosContact(order, SosContactType.LOCAL_EMERGENCY, JP_AMBULANCE_NAME, JP_AMBULANCE_TEL, null)
                : new SosContact(order, SosContactType.LOCAL_POLICE, JP_POLICE_NAME, JP_POLICE_TEL, null);
    }

    /**
     * 현위치에서 가장 가까운 공관.
     * 위치를 모르면 대사관, 대사관도 못 찾으면 첫 번째 공관을 쓴다.
     *
     * 실제 총영사관 관할은 최근접이 아니라 행정구역 기준이지만 그 관할표는 API 에 없다.
     * 오사카에서 사고가 났을 때 도쿄 대사관을 띄우는 것보다는 최근접 근사가 낫다.
     */
    private Optional<OverseasMission> findNearestMission(String countryCode, GeoPoint location) {
        List<OverseasMission> missions = missionRepository.findByCountry_Code(countryCode);
        if (missions.isEmpty()) {
            return Optional.empty();
        }

        if (location != null && location.isValid()) {
            Optional<OverseasMission> nearest = missions.stream()
                    .filter(mission -> mission.getLatitude() != null && mission.getLongitude() != null)
                    .min(Comparator.comparingDouble(mission -> GeoDistance.kilometersBetween(
                            location.latitude(), location.longitude(),
                            mission.getLatitude(), mission.getLongitude())));
            if (nearest.isPresent()) {
                return nearest;
            }
        }

        return missions.stream()
                .filter(mission -> MISSION_TYPE_EMBASSY.equals(mission.getMissionTypeCode()))
                .findFirst()
                .or(() -> missions.stream().findFirst());
    }

    /*
     * 긴급연락처가 없으면 대표번호로 폴백한다.
     * 긴급연락처 자리에 서울 영사콜센터가 들어있는 공관(니가타·삿포로·요코하마)은
     * 현지 번호로 알고 걸면 요금이 나가므로 주의 문구를 붙인다.
     */
    private SosContact toContact(OverseasMission mission, int order) {
        boolean hasUrgency = mission.getUrgencyTelNo() != null && !mission.getUrgencyTelNo().isBlank();
        String phone = hasUrgency ? mission.getUrgencyTelNo() : mission.getTelNo();
        String note = (hasUrgency && !mission.isUrgencyLocal()) ? NOTE_INTERNATIONAL_CALL : null;

        return new SosContact(order, SosContactType.EMBASSY, mission.getNameKo(), phone, note);
    }
}
