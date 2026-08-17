package com.medipass.server.domain.sos.exception;

import com.medipass.server.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SosErrorCode implements BaseResponseCode {

    SCRIPT_LANGUAGE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "SOS_400_1",
            "아직 현지어 설명문을 지원하지 않는 국가입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
