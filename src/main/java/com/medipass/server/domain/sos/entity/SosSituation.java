package com.medipass.server.domain.sos.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SOS 긴급 도움 상황 4종
 * 상황에 따라 연락처 1번 줄(경찰/응급)이 달라진다
 */
@Getter
@RequiredArgsConstructor
public enum SosSituation {

    MEDICATION_LOST("약을 잃어버렸어요", false),
    CUSTOMS_CHECK("세관 혹은 경찰의 확인을 받고 있어요", false),
    MEDICATION_SHORTAGE("가져온 약이 부족해요", true),
    EMERGENCY_SYMPTOM("응급 증상이 있어요", false);

    // 상단 빨간 배너 문구
    private final String label;

    /*
     * 연락처 1번 줄에 경찰 대신 응급번호를 쓰는지.
     * 디자인상 '약 부족'만 응급번호이고 '응급 증상'은 경찰인데, 응급 증상 화면의
     * 대응 순서 1번이 "즉시 현지 응급번호로 연결하기" 라 둘이 뒤바뀐 것으로 보인다.
     * 확인 전까지는 디자인 그대로 둔다.
     */
    private final boolean usesAmbulance;
}
