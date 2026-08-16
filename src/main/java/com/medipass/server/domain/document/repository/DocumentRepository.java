package com.medipass.server.domain.document.repository;

import com.medipass.server.domain.document.entity.Document;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByChecklistItem_Id(Long checklistItemId);

    // 서류 보기 — 사용자 소유 여행의 서류와 화면 표시 정보를 함께 조회한다.
    @EntityGraph(attributePaths = {
            "checklistItem.requirementTemplate",
            "checklistItem.tripMedication.medication.product"
    })
    Optional<Document> findByIdAndChecklistItem_TripMedication_Trip_User_Id(
            Long documentId,
            Long userId
    );

    boolean existsByChecklistItem_Id(Long checklistItemId);
}
