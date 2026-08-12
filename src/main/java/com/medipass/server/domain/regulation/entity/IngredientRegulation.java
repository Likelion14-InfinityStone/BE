package com.medipass.server.domain.regulation.entity;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 국가별 성분 규정 성분 base INN 기준으로 해당 국가의 반입 준비 단계를 판정
 * medication_ingredient.inn 과 (country_code, inn) 으로 매칭
 * 규정 목록에 없는 성분은 여기에 행이 없으며, 그 경우 ALLOWED 로 간주
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "ingredient_regulation",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ingredient_regulation_country_substance",
                columnNames = {"country_code", "substance_name"}
        ),
        indexes = @Index(name = "idx_ingredient_regulation_country", columnList = "country_code")
)
public class IngredientRegulation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 규정이 적용되는 국가
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    // 원문 물질명 (PDF 그대로, 근거·표시용)
    @Column(name = "substance_name", nullable = false)
    private String substanceName;

    // 성분 base INN (매칭용) — 정규화 단계에서 채움, 그전엔 null
    @Column(name = "inn")
    private String inn;

    // 국가별 원천 분류코드 서류 템플릿 매핑에 사용
    @Column(name = "category_code", length = 50)
    private String categoryCode;

    // 반입 준비 단계
    @Enumerated(EnumType.STRING)
    @Column(name = "preparation_level")
    private PreparationLevel preparationLevel;

    // 향정신성 반입 허용 한도 (mg 단위로 통일, P만) — 초과 시 진단서 필요, 없으면 null
    @Column(name = "limit_mg", precision = 12, scale = 4)
    private BigDecimal limitMg;

    // 준비사항·분류 설명 (사용자 표시)
    @Column(name = "requirement_summary", columnDefinition = "TEXT")
    private String requirementSummary;

    // 근거 원문 URL (근거 스탬프)
    @Column(name = "source_url")
    private String sourceUrl;

    // 공식 확인일 (근거 스탬프)
    @Column(name = "verified_date")
    private LocalDate verifiedDate;

    // 활성 여부 최신 목록에서 빠진 물질은 삭제 대신 false 로 비활성 처리
    @Column(name = "active", nullable = false)
    private boolean active;

    // -------------------- 메서드 --------------------

    /** 최신 데이터로 기존 행을 갱신 (id 유지) */
    public void applyUpdate(String categoryCode, PreparationLevel preparationLevel,
                            BigDecimal limitMg, String sourceUrl, LocalDate verifiedDate) {
        this.categoryCode = categoryCode;
        this.preparationLevel = preparationLevel;
        this.limitMg = limitMg;
        this.sourceUrl = sourceUrl;
        this.verifiedDate = verifiedDate;
        this.active = true;
    }

    /** 목록에서 빠진 물질 비활성 처리 */
    public void deactivate() {
        this.active = false;
    }
}
