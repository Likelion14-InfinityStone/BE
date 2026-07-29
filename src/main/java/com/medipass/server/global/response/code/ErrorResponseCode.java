package com.medipass.server.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorResponseCode implements BaseResponseCode {
    // 400 Bad Request
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "GLOBAL_400_1", "잘못된 요청입니다."),
    INVALID_HTTP_MESSAGE_BODY(HttpStatus.BAD_REQUEST, "GLOBAL_400_2", "HTTP 요청 바디의 형식이 잘못되었습니다."),
    INVALID_HTTP_MESSAGE_PARAMETER(HttpStatus.BAD_REQUEST, "GLOBAL_400_3", "HTTP 요청 파라미터 형식이 잘못되었습니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "GLOBAL_401_1", "인증이 필요합니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "GLOBAL_401_2", "만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "GLOBAL_401_3", "유효하지 않은 토큰입니다."),

    // 403 Forbidden
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "GLOBAL_403", "해당 요청에 접근 권한이 없습니다."),

    // 404 Not Found
    NOT_FOUND_ENDPOINT(HttpStatus.NOT_FOUND, "GLOBAL_404_1", "존재하지 않는 엔드포인트입니다. 요청 URL을 확인해주세요."),
    NOT_FOUND_RESOURCE(HttpStatus.NOT_FOUND, "GLOBAL_404_2", "요청한 리소스를 찾을 수 없습니다."),

    // 405 Method Not Allowed
    UNSUPPORTED_HTTP_METHOD(HttpStatus.METHOD_NOT_ALLOWED, "GLOBAL_405", "지원하지 않는 HTTP 메서드입니다."),

    // 500 Internal Server Error
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_500", "서버 내부에서 알 수 없는 에러가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
