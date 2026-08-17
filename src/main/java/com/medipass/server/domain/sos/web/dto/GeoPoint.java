package com.medipass.server.domain.sos.web.dto;

import java.math.BigDecimal;

/**
 * 사용자의 현재 위치 — 가장 가까운 재외공관을 고르는 데만 쓴다
 * 위치 권한을 거부하면 null 로 오며, 그때는 대사관을 기본값으로 쓴다
 */
public record GeoPoint(
        BigDecimal latitude,
        BigDecimal longitude
) {
    public boolean isValid() {
        return latitude != null && longitude != null;
    }
}
