package com.medipass.server.domain.medication.web.dto;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.domain.medication.entity.DoseUnit;
import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.domain.medication.entity.MfdsProduct;
import com.medipass.server.domain.trip.entity.TripMedication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record MedicationCardDetailRes(
        String nickname,
        Long medicationId,
        Front front,
        Back back
) {

    public static MedicationCardDetailRes from(
            String nickname,
            Medication medication,
            List<TripMedication> tripMedications,
            MedicationCardLanguage language,
            String issuer
    ) {
        MfdsProduct product = medication.getProduct();
        String productName = language == MedicationCardLanguage.EN
                ? product.getProductEnName()
                : product.getProductKoName();

        return new MedicationCardDetailRes(
                nickname,
                medication.getId(),
                new Front(productName),
                new Back(
                        productName,
                        medication.getDispensedAt(),
                        issuer,
                        List.copyOf(product.getIngredients()),
                        product.getContentMg(),
                        medication.getIntakesPerDay(),
                        medication.getTotalDays(),
                        medication.getDosePerIntake(),
                        medication.getDoseUnit(),
                        connectedCountries(tripMedications, language)
                )
        );
    }

    private static List<ConnectedCountry> connectedCountries(
            List<TripMedication> tripMedications,
            MedicationCardLanguage language
    ) {
        Map<String, Country> countriesByCode = new LinkedHashMap<>();
        for (TripMedication tripMedication : tripMedications) {
            Country country = tripMedication.getTrip().getDestinationCountry();
            countriesByCode.putIfAbsent(country.getCode(), country);
        }
        return countriesByCode.values().stream()
                .map(country -> ConnectedCountry.from(country, language))
                .toList();
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
        private static ConnectedCountry from(Country country, MedicationCardLanguage language) {
            String localizedName = language == MedicationCardLanguage.EN
                    ? country.getNameEn()
                    : country.getNameKo();
            return new ConnectedCountry(country.getCode(), localizedName);
        }
    }
}
