package com.medipass.server.domain.sos.entity;

/**
 * 결과 화면 연락처 줄의 종류 — 프론트가 아이콘·동작을 구분하는 데 쓴다
 */
public enum SosContactType {
    LOCAL_POLICE,     // 현지 경찰서
    LOCAL_EMERGENCY,  // 현지 응급 번호
    EMBASSY,          // 재외공관 (현위치에서 가장 가까운 곳)
    MEDICAL_INFO      // 의료기관 안내센터
}
