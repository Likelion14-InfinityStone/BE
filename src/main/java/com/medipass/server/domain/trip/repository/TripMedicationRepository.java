package com.medipass.server.domain.trip.repository;

import com.medipass.server.domain.trip.entity.TripMedication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripMedicationRepository extends JpaRepository<TripMedication, Long> {

    // 여행 상세 — 그 여행에 담긴 약 목록
    List<TripMedication> findByTrip_Id(Long tripId);

    // 홈 복약카드 — 현재 페이지의 약과 연결된 도착 국가를 한 번에 조회한다.
    @EntityGraph(attributePaths = {"medication", "trip.destinationCountry"})
    List<TripMedication> findByMedication_IdInAndMedication_User_Id(
            List<Long> medicationIds,
            Long userId
    );
}
