package com.medipass.server.global.mofa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * 외교부 재외공관 1건
 *
 * 응답에 오는 필드 중 안 쓰기로 한 것들 (2026-08-16 JP 응답으로 확인)
 * - center_tel_no : 10곳 중 4곳만 있고 그마저 영사콜센터 번호 중복
 * - free_tel_no   : 무료전화가 아님. 긴급번호와 같은 값이거나 국가번호 없는 한국 번호가 들어있음
 * - country_nm / country_eng_nm : country 테이블에 있음
 * - embassy_manage_ty_cd(_nm)   : 10곳 전부 "일반" 이라 변별력 없음
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MofaMissionItem(
        // 공관 고유코드 (jp, jp-osaka) — 공관명과 달리 표기가 바뀌지 않아 UPSERT 키로 쓴다
        @JsonProperty("embassy_cd") String embassyCd,
        @JsonProperty("country_iso_alp2") String countryCode,
        @JsonProperty("embassy_kor_nm") String nameKo,

        // 10=대사관, 20=총영사관
        @JsonProperty("embassy_ty_cd") String missionTypeCode,
        @JsonProperty("embassy_ty_cd_nm") String missionTypeName,

        // 표기가 +81-3-.. / (81) 92-.. / (82)2-.. 로 섞여 오므로 저장 전에 E.164 로 정규화해야 한다
        @JsonProperty("tel_no") String telNo,
        @JsonProperty("urgency_tel_no") String urgencyTelNo,

        // 문서에는 embassy_addr 로 적혀 있으나 실제로는 이 이름으로 온다
        @JsonProperty("emblgbd_addr") String address,

        // 최근접 공관 계산용 (JSON number 로 오며 소수점 8자리까지 있다)
        @JsonProperty("embassy_lat") BigDecimal latitude,
        @JsonProperty("embassy_lng") BigDecimal longitude
) {
}
