package com.medipass.server.domain.sos.util;

import java.math.BigDecimal;

/**
 * 두 좌표 사이 거리 — 가장 가까운 재외공관을 고르는 데만 쓴다
 * 순위만 매기면 되므로 하버사인 근사로 충분하다
 */
public final class GeoDistance {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoDistance() {
    }

    public static double kilometersBetween(BigDecimal lat1, BigDecimal lng1,
                                           BigDecimal lat2, BigDecimal lng2) {
        double latRad1 = Math.toRadians(lat1.doubleValue());
        double latRad2 = Math.toRadians(lat2.doubleValue());
        double deltaLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double deltaLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(latRad1) * Math.cos(latRad2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
