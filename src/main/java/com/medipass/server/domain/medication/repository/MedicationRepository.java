package com.medipass.server.domain.medication.repository;

import com.medipass.server.domain.medication.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    boolean existsByUser_IdAndProduct_MfdsProductCode(Long userId, String mfdsProductCode);

    // 내 약 보관함 (최근 등록순)
    List<Medication> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
