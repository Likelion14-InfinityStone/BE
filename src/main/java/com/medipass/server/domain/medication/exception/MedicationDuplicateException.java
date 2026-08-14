package com.medipass.server.domain.medication.exception;

import com.medipass.server.global.exception.BaseException;

public class MedicationDuplicateException extends BaseException {

    public MedicationDuplicateException() {
        super(MedicationErrorCode.MEDICATION_DUPLICATE);
    }
}
