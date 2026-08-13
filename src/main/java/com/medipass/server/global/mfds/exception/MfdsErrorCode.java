package com.medipass.server.global.mfds.exception;

import com.medipass.server.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MfdsErrorCode implements BaseResponseCode {

    EMPTY_QUERY(HttpStatus.BAD_REQUEST, "MFDS_400", "검색할 제품명을 입력해주세요."),
    NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "MFDS_500", "식약처 API 인증키가 설정되지 않았습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "MFDS_502", "식약처 API가 일시적으로 응답하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
