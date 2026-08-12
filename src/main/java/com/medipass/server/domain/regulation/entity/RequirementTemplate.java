package com.medipass.server.domain.regulation.entity;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 국가·분류별 필요 서류 템플릿
 * 필요 서류는 물질별이 아니라 (국가, 분류)로 결정되므로, 여기 채워두면 조회 시 category 로 붙여 사용
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "requirement_template",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_requirement_template_country_category_label",
                columnNames = {"country_code", "category_code", "label"}
        ),
        indexes = @Index(name = "idx_requirement_template_country_category",
                columnList = "country_code, category_code")
)
public class RequirementTemplate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 적용 국가 (country.code 참조)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    // 적용 분류코드 (국가별 표기)
    @Column(name = "category_code", length = 50, nullable = false)
    private String categoryCode;

    // 준비항목 성격 (UPLOAD / ACTION)
    @Enumerated(EnumType.STRING)
    @Column(name = "kind")
    private RequirementKind kind;

    // 서류 이름 (화면 표시용)
    @Column(name = "label", nullable = false)
    private String label;

    // 양식 다운로드/안내 링크 (ACTION 일 때) — 정부 공식 양식
    @Column(name = "form_url", length = 500)
    private String formUrl;

    // 안내 설명 (제출처·기한·포함내용 등)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 최신 데이터로 기존 행 갱신 (id 유지)
    public void applyUpdate(RequirementKind kind, String formUrl, String description) {
        this.kind = kind;
        this.formUrl = formUrl;
        this.description = description;
    }
}
