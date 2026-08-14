package com.medipass.server.domain.trip.entity;

import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.domain.regulation.entity.PreparationLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 여정에 챙길 약 (trip ↔ medication + 판정 스냅샷)
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "trip_medication",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_trip_medication_trip_medication",
                columnNames = {"trip_id", "medication_id"}
        )
)
public class TripMedication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속 여정
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    // 챙길 약
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    // 챙기는 일수 (예: 5 = 5일분) — 규제 한도(일수)와 비교, medication 복용정보로 실제량 계산
    @Column(name = "carry_days", nullable = false)
    private Integer carryDays;

    // 판정 결과 스냅샷 (신호등)
    @Enumerated(EnumType.STRING)
    @Column(name = "preparation_level", nullable = false)
    private PreparationLevel preparationLevel;

    // 통제 성분 포함 여부 (판정 스냅샷)
    @Column(name = "regulated", nullable = false, columnDefinition = "boolean default false")
    private boolean regulated;

    // 분류 코드 (N/P/SRM) — 규제 없으면 null
    @Column(name = "category_code", length = 50)
    private String categoryCode;

    // 분류 표시명 (마약류/향정신성의약품/각성제 원료) — 규제 없으면 null
    @Column(name = "category_name")
    private String categoryName;

    // 수량 조건 (예: "최대 2160mg · 30일분") — 규제 없으면 null
    @Column(name = "quantity_condition")
    private String quantityCondition;

    // AI 근거 요약 (근거탭 최초 조회 때 생성해 저장, 이후 재사용)
    @Column(name = "basis_summary", columnDefinition = "TEXT")
    private String basisSummary;

    // 근거 생성 시각
    @Column(name = "basis_generated_at")
    private LocalDateTime basisGeneratedAt;

    // 근거 요약 저장 (생성 시점 함께 기록)
    public void updateBasis(String basisSummary) {
        this.basisSummary = basisSummary;
        this.basisGeneratedAt = LocalDateTime.now();
    }

    // 여정에 챙길 약 한 건 — 일수 + analyze 판정 스냅샷(신호등·통제여부·분류·수량조건)
    public static TripMedication of(Trip trip, Medication medication, Integer carryDays,
                                    PreparationLevel preparationLevel, boolean regulated,
                                    String categoryCode, String categoryName, String quantityCondition) {
        return TripMedication.builder()
                .trip(trip)
                .medication(medication)
                .carryDays(carryDays)
                .preparationLevel(preparationLevel)
                .regulated(regulated)
                .categoryCode(categoryCode)
                .categoryName(categoryName)
                .quantityCondition(quantityCondition)
                .build();
    }
}
