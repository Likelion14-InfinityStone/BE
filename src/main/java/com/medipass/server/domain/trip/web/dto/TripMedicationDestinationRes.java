package com.medipass.server.domain.trip.web.dto;

import com.medipass.server.domain.medication.entity.DoseUnit;
import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.domain.medication.entity.MfdsProduct;
import com.medipass.server.domain.regulation.entity.RequirementKind;
import com.medipass.server.domain.regulation.entity.RequirementTemplate;
import com.medipass.server.domain.trip.entity.ChecklistItem;
import com.medipass.server.domain.trip.entity.TripMedication;
import com.medipass.server.domain.trip.util.DosageCalculator;

import java.math.BigDecimal;
import java.util.List;

/**
 * 약 상세 - 목적지 규정 — 헤더(제품·성분·함량·소지량) + 판정 스냅샷 + 필요 서류
 * "통제 성분 여부 / 수량 조건" 라벨은 프론트 고정, 서류 라벨은 requirements[].label 로 내려간다
 */
public record TripMedicationDestinationRes(
        Long tripMedicationId,
        Long medicationId,

        // 헤더
        String productKoName,        // 제품명
        List<String> ingredients,    // 성분 (복수 가능)
        BigDecimal contentMg,        // 함량(mg), 없으면 null
        BigDecimal carryQuantity,    // 소지 수량 (일수 × 하루횟수 × 1회량)
        DoseUnit carryQuantityUnit,  // 소지 수량 단위 (정/캡슐 …)
        Integer carryDays,           // 복용 기간 (일분)
        String destinationNameKo,    // 대상 국가 (일본)

        // 목적지 규정 (값)
        boolean regulated,           // 통제 성분 여부
        String categoryCode,         // 분류 코드 (N/P/SRM), 규제 없으면 null
        String categoryName,         // 분류 표시명 (마약류 등), 규제 없으면 null
        String quantityCondition,    // 수량 조건, 규제 없으면 null

        List<RequirementItem> requirements // 필요 서류 (라벨은 데이터)
) {
    public static TripMedicationDestinationRes from(TripMedication tripMedication, List<ChecklistItem> checklist) {
        Medication medication = tripMedication.getMedication();
        MfdsProduct product = medication.getProduct();
        BigDecimal carryQuantity = DosageCalculator.totalUnits(
                medication.getIntakesPerDay(), medication.getDosePerIntake(), tripMedication.getCarryDays());

        return new TripMedicationDestinationRes(
                tripMedication.getId(),
                medication.getId(),
                product.getProductKoName(),
                product.getIngredients(),
                product.getContentMg(),
                carryQuantity,
                medication.getDoseUnit(),
                tripMedication.getCarryDays(),
                tripMedication.getTrip().getDestinationCountry().getNameKo(),
                tripMedication.isRegulated(),
                tripMedication.getCategoryCode(),
                tripMedication.getCategoryName(),
                tripMedication.getQuantityCondition(),
                checklist.stream().map(RequirementItem::from).toList()
        );
    }

    public record RequirementItem(
            Long templateId,
            RequirementKind kind,
            String label,
            String formUrl,
            String description
    ) {
        private static RequirementItem from(ChecklistItem checklistItem) {
            RequirementTemplate template = checklistItem.getRequirementTemplate();
            return new RequirementItem(
                    template.getId(), template.getKind(), template.getLabel(),
                    template.getFormUrl(), template.getDescription());
        }
    }
}
