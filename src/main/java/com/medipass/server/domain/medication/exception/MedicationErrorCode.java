package com.medipass.server.domain.medication.exception;

import com.medipass.server.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MedicationErrorCode implements BaseResponseCode {

    MEDICATION_PRODUCT_MISMATCH("MEDICATION_400_1", HttpStatus.BAD_REQUEST, "스캔한 의약품 정보와 등록 요청이 일치하지 않습니다."),
    MEDICATION_PRODUCT_NOT_FOUND("MEDICATION_404_1", HttpStatus.NOT_FOUND, "등록할 의약품 정보를 찾을 수 없습니다."),
    MEDICATION_DUPLICATE("MEDICATION_409_1", HttpStatus.CONFLICT, "이미 등록된 의약품입니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
