package com.medipass.server.domain.medication.repository;

import com.medipass.server.domain.medication.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
}
