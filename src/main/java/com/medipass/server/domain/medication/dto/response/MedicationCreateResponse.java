package com.medipass.server.domain.medication.dto.response;

import com.medipass.server.domain.medication.entity.Medication;
import java.util.List;

public record MedicationCreateResponse(
        List<Item> medications
) {

    public static MedicationCreateResponse from(List<Medication> medications) {
        return new MedicationCreateResponse(
                medications.stream().map(Item::from).toList()
        );
    }

    public record Item(
            Long medicationId,
            String productKoName
    ) {
        private static Item from(Medication medication) {
            return new Item(
                    medication.getId(),
                    medication.getProductKoName()
            );
        }
    }
}
