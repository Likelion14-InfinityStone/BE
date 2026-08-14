package com.medipass.server.domain.medication.exception;

import com.medipass.server.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MedicationErrorCode implements BaseResponseCode {

    MEDICATION_DUPLICATE(
            "MEDICATION_409_1",
            HttpStatus.CONFLICT,
            "이미 등록된 의약품입니다."
    );

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
