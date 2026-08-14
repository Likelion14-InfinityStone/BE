package com.medipass.server.domain.trip.entity;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.domain.user.entity.User;
import com.medipass.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 여정 (출발국 → 도착국)
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "trip")
public class Trip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 여행을 등록한 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 여행 제목 — 미입력 시 "{도착국} 여행{N}"으로 자동 생성 (null 불가)
    @Column(name = "title", nullable = false)
    private String title;

    // 출발국 (KR) — 2/3자리 코드·국가명은 Country 마스터에서
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_country", nullable = false)
    private Country originCountry;

    // 출발 공항코드 (ICN)
    @Column(name = "origin_airport_code", length = 10, nullable = false)
    private String originAirportCode;

    // 출발 도시 (인천)
    @Column(name = "origin_city", nullable = false)
    private String originCity;

    // 도착국 (JP)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_country", nullable = false)
    private Country destinationCountry;

    // 도착 공항코드 (NRT)
    @Column(name = "destination_airport_code", length = 10, nullable = false)
    private String destinationAirportCode;

    // 도착 도시 (나리타)
    @Column(name = "destination_city", nullable = false)
    private String destinationCity;

    // 출국일
    @Column(name = "depart_on", nullable = false)
    private LocalDate departOn;

    // 귀국일
    @Column(name = "return_on", nullable = false)
    private LocalDate returnOn;

    // 여정 생성 — 출발/도착 국가·공항·도시와 일정으로
    public static Trip of(
            User user, String title,
            Country originCountry, String originAirportCode, String originCity,
            Country destinationCountry, String destinationAirportCode, String destinationCity,
            LocalDate departOn, LocalDate returnOn
    ) {
        return Trip.builder()
                .user(user)
                .title(title)
                .originCountry(originCountry)
                .originAirportCode(originAirportCode)
                .originCity(originCity)
                .destinationCountry(destinationCountry)
                .destinationAirportCode(destinationAirportCode)
                .destinationCity(destinationCity)
                .departOn(departOn)
                .returnOn(returnOn)
                .build();
    }

    // 여행 제목 변경 (중복 검사는 서비스에서)
    public void updateTitle(String title) {
        this.title = title;
    }
}
