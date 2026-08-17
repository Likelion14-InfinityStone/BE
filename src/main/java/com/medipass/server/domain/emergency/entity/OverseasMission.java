package com.medipass.server.domain.emergency.entity;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 재외공관 (대사관·총영사관) — 외교부 공공데이터 API 응답을 캐싱한 것
 * 응급 화면이 외부 API 지연·장애에 물리면 안 되므로 조회는 항상 이 테이블만 본다
 * 국가당 공관이 여러 개라(일본 10곳) 화면에는 현위치 기준 최근접 1곳만 노출한다
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "overseas_mission",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_overseas_mission_embassy_cd",
                columnNames = "embassy_cd"
        ),
        indexes = @Index(name = "idx_overseas_mission_country", columnList = "country_code")
)
public class OverseasMission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외교부가 부여한 공관 고유코드 (jp, jp-osaka) — 공관명과 달리 표기가 바뀌지 않아 UPSERT 키로 쓴다
    @Column(name = "embassy_cd", nullable = false, length = 40)
    private String embassyCd;

    // 공관이 있는 국가
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    // 공관명 (주 오사카 대한민국 총영사관)
    @Column(name = "name_ko", nullable = false)
    private String nameKo;

    // 공관 유형코드 (10=대사관, 20=총영사관) — 외부 코드라 enum 대신 원문 유지
    @Column(name = "mission_type_code", length = 10)
    private String missionTypeCode;

    // 공관 유형명 (대사관, 총영사관)
    @Column(name = "mission_type_name", length = 30)
    private String missionTypeName;

    // 대표번호 (근무시간 중) — E.164 정규화 후 저장
    @Column(name = "tel_no", length = 30)
    private String telNo;

    // 긴급연락처 (24시간) — E.164 정규화 후 저장
    @Column(name = "urgency_tel_no", length = 30)
    private String urgencyTelNo;

    /*
     * 긴급연락처가 현지 번호인지 여부.
     * 니가타·삿포로·요코하마처럼 이 자리에 한국 영사콜센터(+82-2-3210-0404)가 들어오는 공관이 있다.
     * 현지 번호인 줄 알고 걸면 국제전화 요금이 나가므로 화면 라벨을 구분해야 한다.
     */
    @Column(name = "urgency_local", nullable = false)
    private boolean urgencyLocal;

    // 공관 주소 (원문 표기가 영문·현지어로 섞여 있어 가공하지 않고 그대로 둔다)
    @Column(name = "address")
    private String address;

    // 최근접 공관 계산용 좌표
    @Column(name = "latitude", precision = 12, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 12, scale = 8)
    private BigDecimal longitude;

    // -------------------- 메서드 --------------------

    /** 최신 데이터로 기존 행을 갱신 (id 유지) */
    public void applyUpdate(String nameKo, String missionTypeCode, String missionTypeName,
                            String telNo, String urgencyTelNo, boolean urgencyLocal,
                            String address, BigDecimal latitude, BigDecimal longitude) {
        this.nameKo = nameKo;
        this.missionTypeCode = missionTypeCode;
        this.missionTypeName = missionTypeName;
        this.telNo = telNo;
        this.urgencyTelNo = urgencyTelNo;
        this.urgencyLocal = urgencyLocal;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
