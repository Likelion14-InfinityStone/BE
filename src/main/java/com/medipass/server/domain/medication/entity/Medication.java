package com.medipass.server.domain.medication.entity;

import com.medipass.server.domain.user.entity.User;
import com.medipass.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 사용자가 OCR 결과를 확인한 뒤 등록한 확정 의약품.
 * 식약처 품목코드를 제품 식별자로 보관하고 처방 당시 복용 정보를 함께 저장한다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "medication",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_medication_user_mfds_product",
                columnNames = {"user_id", "mfds_product_code"}
        )
)
public class Medication extends BaseEntity {

    // 서비스 내부 의약품 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 의약품을 등록한 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 품목기준코드(ITEM_SEQ)
    @Column(name = "mfds_product_code", nullable = false, length = 30)
    private String mfdsProductCode;

    // 한글 제품명
    @Column(name = "product_ko_name", nullable = false, length = 300)
    private String productKoName;

    // 영문 제품명, 없으면 null
    @Column(name = "product_en_name", length = 300)
    private String productEnName;

    // 약 봉투 조제일자
    @Column(name = "dispensed_at")
    private LocalDate dispensedAt;

    // 발행기관
    @Column(name = "issuer", length = 200)
    private String issuer;

    // 하루 복용 횟수
    @Column(name = "intakes_per_day")
    private Integer intakesPerDay;

    // 총 복용 일수
    @Column(name = "total_days")
    private Integer totalDays;

    // 1회 복용량
    @Column(name = "dose_per_intake", precision = 12, scale = 4)
    private BigDecimal dosePerIntake;

    // 1회 복용량의 단위(정, 캡슐, 포 등)
    @Enumerated(EnumType.STRING)
    @Column(name = "dose_unit", length = 20)
    private DoseUnit doseUnit;

    public static Medication create(
            User user,
            String mfdsProductCode,
            String productKoName,
            String productEnName,
            LocalDate dispensedAt,
            String issuer,
            Integer intakesPerDay,
            Integer totalDays,
            BigDecimal dosePerIntake,
            DoseUnit doseUnit
    ) {
        return Medication.builder()
                .user(user)
                .mfdsProductCode(mfdsProductCode)
                .productKoName(productKoName)
                .productEnName(productEnName)
                .dispensedAt(dispensedAt)
                .issuer(issuer)
                .intakesPerDay(intakesPerDay)
                .totalDays(totalDays)
                .dosePerIntake(dosePerIntake)
                .doseUnit(doseUnit)
                .build();
    }
}
