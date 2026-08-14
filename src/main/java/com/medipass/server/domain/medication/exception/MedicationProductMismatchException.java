package com.medipass.server.domain.medication.exception;

import com.medipass.server.global.exception.BaseException;

public class MedicationProductMismatchException extends BaseException {

    public MedicationProductMismatchException() {
        super(MedicationErrorCode.MEDICATION_PRODUCT_MISMATCH);
    }
}
