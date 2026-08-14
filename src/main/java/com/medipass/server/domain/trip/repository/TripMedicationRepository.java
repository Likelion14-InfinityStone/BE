package com.medipass.server.domain.trip.repository;

import com.medipass.server.domain.trip.entity.TripMedication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripMedicationRepository extends JpaRepository<TripMedication, Long> {

    // 여행 상세 — 그 여행에 담긴 약 목록
    List<TripMedication> findByTrip_Id(Long tripId);
}
