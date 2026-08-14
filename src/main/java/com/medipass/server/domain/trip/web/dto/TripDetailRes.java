package com.medipass.server.domain.trip.web.dto;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.domain.regulation.entity.PreparationLevel;
import com.medipass.server.domain.trip.entity.Trip;
import com.medipass.server.domain.trip.entity.TripMedication;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 여행 상세 — 여행 티켓(카드) + 담긴 약 목록(신호등 포함)
 */
public record TripDetailRes(
        Long tripId,
        String title,
        long dday,           // 출국까지 남은 일수 (음수면 지난 여행)
        Endpoint origin,
        Endpoint destination,
        LocalDate departOn,
        LocalDate returnOn,
        List<MedicationItem> medications
) {
    public static TripDetailRes from(Trip trip, List<TripMedication> tripMedications, LocalDate today) {
        return new TripDetailRes(
                trip.getId(),
                trip.getTitle(),
                ChronoUnit.DAYS.between(today, trip.getDepartOn()),
                Endpoint.from(trip.getOriginCountry(), trip.getOriginAirportCode(), trip.getOriginCity()),
                Endpoint.from(trip.getDestinationCountry(), trip.getDestinationAirportCode(), trip.getDestinationCity()),
                trip.getDepartOn(),
                trip.getReturnOn(),
                tripMedications.stream().map(MedicationItem::from).toList()
        );
    }

    // 출발·도착 공항 표기 (ICN / KOR / 한국 / 인천)
    public record Endpoint(
            String airportCode,
            String codeAlpha3,
            String countryNameKo,
            String city
    ) {
        private static Endpoint from(Country country, String airportCode, String city) {
            return new Endpoint(airportCode, country.getCodeAlpha3(), country.getNameKo(), city);
        }
    }

    // 여행에 담긴 약 한 건 (신호등 = 저장 당시 판정)
    public record MedicationItem(
            Long tripMedicationId,
            Long medicationId,
            String productKoName,
            Integer carryDays,               // 챙기는 일수
            PreparationLevel preparationLevel // 신호등
    ) {
        private static MedicationItem from(TripMedication tripMedication) {
            return new MedicationItem(
                    tripMedication.getId(),
                    tripMedication.getMedication().getId(),
                    tripMedication.getMedication().getProduct().getProductKoName(),
                    tripMedication.getCarryDays(),
                    tripMedication.getPreparationLevel()
            );
        }
    }
}
