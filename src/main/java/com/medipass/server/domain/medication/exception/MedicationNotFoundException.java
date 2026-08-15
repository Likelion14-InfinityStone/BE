package com.medipass.server.domain.medication.exception;

import com.medipass.server.global.exception.BaseException;

public class MedicationNotFoundException extends BaseException {

    public MedicationNotFoundException() {
        super(MedicationErrorCode.MEDICATION_NOT_FOUND);
    }
}
