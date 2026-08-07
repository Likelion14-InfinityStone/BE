package com.medipass.server.global.ocr.exception;

import com.medipass.server.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OcrErrorCode implements BaseResponseCode {
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "OCR_400_1", "이미지 파일이 비어 있습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "OCR_400_2", "JPEG 또는 PNG 이미지만 업로드할 수 있습니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "OCR_400_3", "이미지 파일은 10MB 이하여야 합니다."),
    OCR_NOT_RECOGNIZED(HttpStatus.UNPROCESSABLE_CONTENT, "OCR_422", "이미지에서 텍스트를 인식하지 못했습니다."),
    OCR_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "OCR_502", "OCR 서비스가 일시적으로 응답하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
