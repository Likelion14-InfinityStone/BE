package com.medipass.server.domain.trip.web.dto;

import com.medipass.server.domain.regulation.entity.RequirementKind;
import com.medipass.server.domain.regulation.entity.RequirementTemplate;
import com.medipass.server.domain.trip.entity.ChecklistItem;

import java.util.List;

/**
 * 약 상세 - 체크리스트 — 준비 서류 목록 + 진행률(doneCount/totalCount)
 * 서류 라벨·성격은 데이터, done 은 사용자가 체크한 상태
 */
public record TripMedicationChecklistRes(
        Long tripMedicationId,
        int doneCount,       // 완료 개수
        int totalCount,      // 전체 개수 (진행률 = doneCount / totalCount)
        List<Item> items
) {
    public static TripMedicationChecklistRes from(Long tripMedicationId, List<ChecklistItem> checklist) {
        int doneCount = (int) checklist.stream().filter(ChecklistItem::isDone).count();
        return new TripMedicationChecklistRes(
                tripMedicationId,
                doneCount,
                checklist.size(),
                checklist.stream().map(Item::from).toList()
        );
    }

    public record Item(
            Long checklistItemId,
            RequirementKind kind,   // UPLOAD(업로드) / ACTION(신청·문의)
            String label,           // 서류명
            String description,     // 안내 설명
            String formUrl,         // 양식·신청 링크 (없으면 null)
            boolean done            // 체크 여부
    ) {
        private static Item from(ChecklistItem checklistItem) {
            RequirementTemplate template = checklistItem.getRequirementTemplate();
            return new Item(
                    checklistItem.getId(),
                    template.getKind(),
                    template.getLabel(),
                    template.getDescription(),
                    template.getFormUrl(),
                    checklistItem.isDone()
            );
        }
    }
}
