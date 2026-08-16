package com.medipass.server.global.s3.exception;

import com.medipass.server.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum S3ErrorCode implements BaseResponseCode {
    STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_1", "파일 저장소 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
