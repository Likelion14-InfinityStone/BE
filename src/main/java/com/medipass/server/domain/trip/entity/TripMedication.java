package com.medipass.server.domain.trip.entity;

import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.domain.regulation.entity.PreparationLevel;
import jakarta.persistence.*;
import lombok.*;

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

    // 여정에 챙길 약 한 건 — 일수와 판정 결과(신호등)로
    public static TripMedication of(Trip trip, Medication medication, Integer carryDays, PreparationLevel preparationLevel) {
        return TripMedication.builder()
                .trip(trip)
                .medication(medication)
                .carryDays(carryDays)
                .preparationLevel(preparationLevel)
                .build();
    }
}
