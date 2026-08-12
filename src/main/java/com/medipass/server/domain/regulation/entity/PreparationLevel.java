package com.medipass.server.domain.regulation.entity;

/**
 * 반입 준비 단계
 * */
public enum PreparationLevel {
    ALLOWED, // 목록에 없음 = 그냥 반입 가능
    PREP_REQUIRED, // 서류·허가 준비 필요
    NOT_ALLOWED // 반입 불가
}
