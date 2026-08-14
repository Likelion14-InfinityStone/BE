package com.medipass.server.domain.medication.web.dto;

import com.medipass.server.domain.medication.entity.Medication;

import java.util.List;

/**
 * 내 약 보관함 — 여행 등록 중 가져갈 약을 고를 때 보여줄 복약카드 목록 (id + 이름)
 */
public record MedicationListRes(
        List<Item> medications
) {

    public static MedicationListRes from(List<Medication> medications) {
        return new MedicationListRes(
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
