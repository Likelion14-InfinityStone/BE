package com.medipass.server.global.mofa.exception;

import com.medipass.server.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MofaErrorCode implements BaseResponseCode {

    NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "MOFA_500", "외교부 API 인증키가 설정되지 않았습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "MOFA_502", "외교부 API가 일시적으로 응답하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
