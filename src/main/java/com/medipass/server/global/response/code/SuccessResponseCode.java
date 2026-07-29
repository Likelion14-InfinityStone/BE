package com.medipass.server.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessResponseCode implements BaseResponseCode {
    SUCCESS_OK(HttpStatus.OK, "GLOBAL_200", "호출에 성공했습니다."),
    SUCCESS_CREATED(HttpStatus.CREATED, "GLOBAL_201", "리소스가 성공적으로 생성되었습니다."),
    SUCCESS_NO_CONTENT(HttpStatus.NO_CONTENT, "GLOBAL_204", "요청은 성공했으나 응답할 콘텐츠가 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
