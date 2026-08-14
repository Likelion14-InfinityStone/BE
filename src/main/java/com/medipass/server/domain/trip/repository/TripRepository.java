package com.medipass.server.domain.trip.repository;

import com.medipass.server.domain.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    // 여행 목록 (출국일 순)
    List<Trip> findByUser_IdOrderByDepartOnAsc(Long userId);

    // 제목 자동생성 시 중복 확인
    boolean existsByUser_IdAndTitle(Long userId, String title);
}
