package com.medipass.server.domain.trip.exception;

import com.medipass.server.global.exception.BaseException;

/**
 * 여행에 담긴 약(trip_medication)이 없거나 해당 여행 소속이 아닐 때 (404)
 */
public class TripMedicationNotFoundException extends BaseException {

    public TripMedicationNotFoundException() {
        super(TripErrorCode.TRIP_MEDICATION_NOT_FOUND);
    }
}
