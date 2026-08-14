package com.medipass.server.domain.trip.exception;

import com.medipass.server.global.exception.BaseException;

/**
 * 본인 여행이 아닌 것에 접근/수정하려 할 때 (403)
 */
public class TripAccessDeniedException extends BaseException {

    public TripAccessDeniedException() {
        super(TripErrorCode.TRIP_ACCESS_DENIED);
    }
}
