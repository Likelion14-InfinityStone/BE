package com.medipass.server.domain.trip.exception;

import com.medipass.server.global.exception.BaseException;

/**
 * 여행이 없거나 본인 여행이 아닐 때 (404)
 */
public class TripNotFoundException extends BaseException {

    public TripNotFoundException() {
        super(TripErrorCode.TRIP_NOT_FOUND);
    }
}
