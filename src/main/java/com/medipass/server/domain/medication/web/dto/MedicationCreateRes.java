package com.medipass.server.domain.medication.web.dto;

import com.medipass.server.domain.medication.entity.Medication;
import java.util.List;

public record MedicationCreateRes(
        List<Item> medications,
        List<SkippedMedication> skippedMedications
) {

    public static MedicationCreateRes from(
            List<Medication> medications,
            List<SkippedMedication> skippedMedications
    ) {
        return new MedicationCreateRes(
                medications.stream().map(Item::from).toList(),
                skippedMedications
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

    public record SkippedMedication(
            String mfdsProductCode,
            String productKoName
    ) {
        public static SkippedMedication duplicate(MedicationCreateReq.Item item) {
            return new SkippedMedication(
                    item.mfdsProductCode().trim(),
                    item.productKoName().trim()
            );
        }
    }
}
