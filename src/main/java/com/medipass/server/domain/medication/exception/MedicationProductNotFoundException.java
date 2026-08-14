package com.medipass.server.domain.medication.exception;

import com.medipass.server.global.exception.BaseException;

public class MedicationProductNotFoundException extends BaseException {

    public MedicationProductNotFoundException() {
        super(MedicationErrorCode.MEDICATION_PRODUCT_NOT_FOUND);
    }
}
