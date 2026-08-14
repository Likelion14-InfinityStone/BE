package com.medipass.server.domain.trip.exception;

import com.medipass.server.global.exception.BaseException;

/**
 * 체크리스트 항목이 없거나 해당 여행-약 소속이 아닐 때 (404)
 */
public class ChecklistItemNotFoundException extends BaseException {

    public ChecklistItemNotFoundException() {
        super(TripErrorCode.CHECKLIST_ITEM_NOT_FOUND);
    }
}
