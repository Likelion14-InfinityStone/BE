package com.medipass.server.global.openai.exception;

import com.medipass.server.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OpenAiErrorCode implements BaseResponseCode {

    NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "OPENAI_500", "OpenAI 설정이 없습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "OPENAI_502", "AI 처리 요청에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
