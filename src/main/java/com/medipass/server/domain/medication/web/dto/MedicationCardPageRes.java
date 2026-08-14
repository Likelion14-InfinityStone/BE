package com.medipass.server.domain.medication.web.dto;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.domain.medication.entity.DoseUnit;
import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.domain.medication.entity.MfdsProduct;
import com.medipass.server.domain.trip.entity.TripMedication;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 가로 스와이프에 필요한 복약카드 앞면·한글 뒷면 정보와 페이지 정보.
 */
public record MedicationCardPageRes(
        String nickname,
        List<Card> cards,
        int page,
        int size,
        boolean hasNext,
        long totalElements
) {

    public static MedicationCardPageRes from(
            String nickname,
            Page<Medication> medications,
            Map<Long, List<TripMedication>> tripMedicationsByMedicationId
    ) {
        return new MedicationCardPageRes(
                nickname,
                medications.getContent().stream()
                        .map(medication -> Card.from(
                                medication,
                                tripMedicationsByMedicationId.getOrDefault(medication.getId(), List.of())
                        ))
                        .toList(),
                medications.getNumber(),
                medications.getSize(),
                medications.hasNext(),
                medications.getTotalElements()
        );
    }

    public record Card(
            Long medicationId,
            Front front,
            Back back
    ) {
        private static Card from(Medication medication, List<TripMedication> tripMedications) {
            MfdsProduct product = medication.getProduct();
            return new Card(
                    medication.getId(),
                    new Front(product.getProductKoName()),
                    new Back(
                            product.getProductKoName(),
                            medication.getDispensedAt(),
                            medication.getIssuer(),
                            List.copyOf(product.getIngredients()),
                            product.getContentMg(),
                            medication.getIntakesPerDay(),
                            medication.getTotalDays(),
                            medication.getDosePerIntake(),
                            medication.getDoseUnit(),
                            connectedCountries(tripMedications)
                    )
            );
        }

        private static List<ConnectedCountry> connectedCountries(List<TripMedication> tripMedications) {
            Map<String, Country> countriesByCode = new LinkedHashMap<>();
            for (TripMedication tripMedication : tripMedications) {
                Country country = tripMedication.getTrip().getDestinationCountry();
                countriesByCode.putIfAbsent(country.getCode(), country);
            }
            return countriesByCode.values().stream()
                    .map(ConnectedCountry::from)
                    .toList();
        }
    }

    public record Front(String productName) {
    }

    public record Back(
            String productName,
            LocalDate dispensedAt,
            String issuer,
            List<String> ingredients,
            BigDecimal contentMg,
            Integer intakesPerDay,
            Integer totalDays,
            BigDecimal dosePerIntake,
            DoseUnit doseUnit,
            List<ConnectedCountry> connectedCountries
    ) {
    }

    public record ConnectedCountry(String code, String name) {
        private static ConnectedCountry from(Country country) {
            return new ConnectedCountry(country.getCode(), country.getNameKo());
        }
    }
}
