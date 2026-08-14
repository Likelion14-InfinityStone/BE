package com.medipass.server.domain.trip.exception;

import com.medipass.server.global.exception.BaseException;

/**
 * 같은 사용자 안에서 여행 이름이 겹칠 때 (409)
 */
public class TripTitleDuplicateException extends BaseException {

    public TripTitleDuplicateException() {
        super(TripErrorCode.TRIP_TITLE_DUPLICATE);
    }
}
