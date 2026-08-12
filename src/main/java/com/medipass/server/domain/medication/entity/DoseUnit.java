package com.medipass.server.domain.medication.entity;

/** 사용자가 복용량을 입력할 때 선택하는 단위 */
public enum DoseUnit {
    TABLET,  // 정
    CAPSULE, // 캡슐
    PACKET,  // 포
    ML,      // 밀리리터
    DROP,    // 방울
    MG       // 밀리그램
}
