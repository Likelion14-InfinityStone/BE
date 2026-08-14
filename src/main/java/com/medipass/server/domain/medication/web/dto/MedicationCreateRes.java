package com.medipass.server.domain.medication.web.dto;

import com.medipass.server.domain.medication.entity.Medication;
import java.util.List;

public record MedicationCreateRes(
        List<Item> medications
) {

    public static MedicationCreateRes from(List<Medication> medications) {
        return new MedicationCreateRes(
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
                    medication.getProduct().getProductKoName()
            );
        }
    }
}
