package com.medipass.server.domain.trip.web.dto;

import com.medipass.server.domain.trip.entity.Trip;

/**
 * 여행 이름 수정 응답 — 변경된 제목
 */
public record TripTitleUpdateRes(
        Long tripId,
        String title
) {
    public static TripTitleUpdateRes from(Trip trip) {
        return new TripTitleUpdateRes(
                trip.getId(),
                trip.getTitle());
    }
}
