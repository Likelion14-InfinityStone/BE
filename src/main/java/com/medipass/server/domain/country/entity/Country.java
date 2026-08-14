package com.medipass.server.domain.country.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 국가 마스터, ISO 3166-1 alpha-2 코드를 PK로 사용
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "country")
public class Country {
    // ISO 3166-1 alpha-2 (KR, JP, CN ...)
    @Id
    @Column(name = "code", length = 2, nullable = false)
    private String code;

    // ISO 3166-1 alpha-3 (KOR, JPN, CHN ...) — 항공/화면 표기용
    @Column(name = "code_alpha3", length = 3)
    private String codeAlpha3;

    // 한글 국가명 (대한민국, 일본, 중국)
    @Column(name = "name_ko")
    private String nameKo;

    // 영문 국가명 (South Korea, Japan, China)
    @Column(name = "name_en")
    private String nameEn;

    // 현지 언어코드 (ko, ja, zh) — 번역용
    @Column(name = "local_lang")
    private String localLang;
}
