package com.medipass.server.domain.trip.repository;

import com.medipass.server.domain.trip.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    // 여행-약에 필요한 서류(체크리스트) 목록
    List<ChecklistItem> findByTripMedication_Id(Long tripMedicationId);

    // 여행 삭제 시 — 해당 여행-약들의 체크리스트 일괄 삭제
    void deleteByTripMedication_IdIn(List<Long> tripMedicationIds);
}
