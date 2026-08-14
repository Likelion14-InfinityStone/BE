package com.medipass.server.domain.trip.repository;

import com.medipass.server.domain.trip.entity.TripMedication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripMedicationRepository extends JpaRepository<TripMedication, Long> {
}
