package com.medipass.server.domain.medication.repository;

import com.medipass.server.domain.medication.entity.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    boolean existsByUser_IdAndProduct_MfdsProductCode(Long userId, String mfdsProductCode);

    // 내 약 보관함 (최근 등록순)
    List<Medication> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // 홈 화면 복약카드 페이지 — DTO 변환 중 제품명 조회를 위해 product를 함께 로딩한다.
    @EntityGraph(attributePaths = "product")
    Page<Medication> findByUser_Id(Long userId, Pageable pageable);
}
