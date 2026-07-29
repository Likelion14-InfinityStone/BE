package com.medipass.server.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.medipass.server.global.response.code.BaseResponseCode;
import com.medipass.server.global.response.code.SuccessResponseCode;
import lombok.Getter;
import org.slf4j.MDC;

import java.time.LocalDateTime;

/**
 * 모든 API 응답을 감싸는 공통 envelope. 성공이면 result 에 데이터, 실패면 result 에 에러 상세(ErrorDetail)가 담긴다.
 * requestId 는 성공/실패와 무관하게 모든 응답 최상단에 담겨 요청 추적에 쓰인다. (RequestIdFilter 가 MDC 에 넣은 값)
 */
@Getter
@JsonPropertyOrder({"isSuccess", "code", "message", "requestId", "result", "timestamp"})
public class ApiResponse<T> {

    private final boolean isSuccess;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL) // 요청 컨텍스트 밖(스케줄러 등)에서는 없을 수 있어 null 이면 생략
    private final String requestId;

    private final T result;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean isSuccess, String code, String message, T result) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.requestId = MDC.get("requestId"); // RequestIdFilter 가 넣어둔 값
        this.result = result;
        this.timestamp = LocalDateTime.now();
    }

    @JsonProperty("isSuccess")
    public boolean isSuccess() {
        return isSuccess;
    }

    private static <T> ApiResponse<T> of(boolean isSuccess, BaseResponseCode code, T result) {
        return new ApiResponse<>(isSuccess, code.getCode(), code.getMessage(), result);
    }

    // ── 성공 ──

    /** 200 OK + 데이터 */
    public static <T> ApiResponse<T> success(T result) {
        return of(true, SuccessResponseCode.SUCCESS_OK, result);
    }

    /** 성공 코드 지정 + 데이터 (예: 201 Created) */
    public static <T> ApiResponse<T> success(SuccessResponseCode code, T result) {
        return of(true, code, result);
    }

    /** 데이터 없는 단순 성공 */
    public static ApiResponse<Void> success() {
        return of(true, SuccessResponseCode.SUCCESS_OK, null);
    }

    // ── 실패 ──

    /** 단순 실패 (result = null) */
    public static ApiResponse<Object> error(BaseResponseCode code) {
        return of(false, code, null);
    }

    /** 메시지를 직접 덮어쓰는 실패 (result = null) */
    public static ApiResponse<Object> error(BaseResponseCode code, String message) {
        return new ApiResponse<>(false, code.getCode(), message, null);
    }

    /** 에러 상세(필드 에러 등)를 result 에 담는 실패 */
    public static ApiResponse<Object> error(BaseResponseCode code, ErrorDetail detail) {
        return of(false, code, (Object) detail);
    }
}
