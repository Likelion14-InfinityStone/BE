package com.medipass.server.domain.trip.repository;

import com.medipass.server.domain.regulation.entity.RequirementKind;
import com.medipass.server.domain.trip.entity.ChecklistItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    // 여행-약에 필요한 서류(체크리스트) 목록
    List<ChecklistItem> findByTripMedication_Id(Long tripMedicationId);

    Optional<ChecklistItem> findByIdAndTripMedication_IdAndTripMedication_Trip_Id(
            Long checklistItemId,
            Long tripMedicationId,
            Long tripId
    );

    // 서류함 메인 — 사용자의 업로드형 체크리스트와 약·서류 정보를 함께 조회한다.
    @EntityGraph(attributePaths = {
            "tripMedication.medication.product",
            "requirementTemplate",
            "document"
    })
    List<ChecklistItem> findByTripMedication_Trip_User_IdAndRequirementTemplate_KindOrderByTripMedication_Medication_CreatedAtDesc(
            Long userId,
            RequirementKind kind
    );

    // 약품별 서류 목록 — 업로드형 체크리스트와 등록된 서류를 함께 조회한다.
    @EntityGraph(attributePaths = {"requirementTemplate", "document"})
    List<ChecklistItem> findByTripMedication_Medication_IdAndTripMedication_Medication_User_IdAndRequirementTemplate_Kind(
            Long medicationId,
            Long userId,
            RequirementKind kind
    );

    // 여행 삭제 시 — 해당 여행-약들의 체크리스트 일괄 삭제
    void deleteByTripMedication_IdIn(List<Long> tripMedicationIds);
}
