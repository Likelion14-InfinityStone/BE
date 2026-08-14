package com.medipass.server.domain.trip.web.dto;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.domain.trip.entity.Trip;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 여행 체크로그함 — 국가 칩 목록 + 선택 국가의 여행 카드
 * countries: 최신 등록국이 앞(왼쪽) / trips: 선택 국가(기본=최신)의 여행, 출국일 빠른 순
 */
public record TripChecklogRes(
        List<CountryChip> countries,   // 상단 국가 칩 (최신 등록국이 앞)
        String selectedCountryCode,    // 현재 선택 국가 (기본=최신), alpha-2
        List<TripItem> trips           // 선택 국가의 여행 목록
) {
    // 상단 칩 — 국기는 alpha-2(countryCode), 라벨은 nameKo
    public record CountryChip(
            String countryCode,  // JP (alpha-2)
            String nameKo        // 일본
    ) {
        public static CountryChip from(Country country) {
            return new CountryChip(country.getCode(), country.getNameKo());
        }
    }

    public record TripItem(
            Long tripId,
            String title,
            long dday,           // 출국까지 남은 일수 (음수면 지난 여행)
            Endpoint origin,
            Endpoint destination,
            LocalDate departOn,
            LocalDate returnOn
    ) {
        public static TripItem from(Trip trip, LocalDate today) {
            long dday = ChronoUnit.DAYS.between(today, trip.getDepartOn());
            return new TripItem(
                    trip.getId(), trip.getTitle(), dday,
                    Endpoint.from(trip.getOriginCountry(), trip.getOriginAirportCode(), trip.getOriginCity()),
                    Endpoint.from(trip.getDestinationCountry(), trip.getDestinationAirportCode(), trip.getDestinationCity()),
                    trip.getDepartOn(), trip.getReturnOn());
        }
    }

    // 출발·도착 공항 표기 (ICN / KOR / 한국 / 인천)
    public record Endpoint(
            String airportCode,  // ICN
            String codeAlpha3,   // KOR
            String countryNameKo,// 한국
            String city          // 인천
    ) {
        public static Endpoint from(Country country, String airportCode, String city) {
            return new Endpoint(airportCode, country.getCodeAlpha3(), country.getNameKo(), city);
        }
    }
}
