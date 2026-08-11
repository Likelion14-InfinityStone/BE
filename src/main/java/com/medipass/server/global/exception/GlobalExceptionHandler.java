package com.medipass.server.global.exception;

import com.medipass.server.global.response.ApiResponse;
import com.medipass.server.global.response.ErrorDetail;
import com.medipass.server.global.response.FieldError;
import com.medipass.server.global.ocr.exception.OcrErrorCode;
import com.medipass.server.global.response.code.BaseResponseCode;
import com.medipass.server.global.response.code.ErrorResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 전역 예외 처리기. 발생한 예외를 통일된 ApiResponse 형태로 변환한다. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 비즈니스 로직에서 의도적으로 던진 예외 */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException e) {
        log.warn("BaseException: {} - {}", e.getErrorCode().getCode(), e.getMessage());
        return error(e.getErrorCode(), e.getMessage());
    }

    /** @Valid 검증 실패(@RequestBody) — 필드별 상세 포함 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<FieldError> errors = toFieldErrors(e.getBindingResult().getFieldErrors());
        log.warn("MethodArgumentNotValidException: {}", errors);
        return error(ErrorResponseCode.INVALID_HTTP_MESSAGE_BODY, ErrorDetail.of(errors));
    }

    /** 바인딩 실패(@ModelAttribute 등) — 필드별 상세 포함 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException e) {
        List<FieldError> errors = toFieldErrors(e.getBindingResult().getFieldErrors());
        log.warn("BindException: {}", errors);
        return error(ErrorResponseCode.INVALID_HTTP_MESSAGE_BODY, ErrorDetail.of(errors));
    }

    /** 요청 바디(JSON) 파싱 실패 — Enum 오타도 친절히 안내 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        if (isEnumInvalidationError(e)) {
            return error(ErrorResponseCode.INVALID_HTTP_MESSAGE_BODY, "입력된 값이 유효하지 않습니다. Enum 타입의 철자나 대소문자를 확인해주세요.");
        }
        return error(ErrorResponseCode.INVALID_HTTP_MESSAGE_BODY);
    }

    /** 요청 파라미터 타입 변환 실패 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("MethodArgumentTypeMismatchException: {}", e.getMessage());
        return error(ErrorResponseCode.INVALID_HTTP_MESSAGE_PARAMETER);
    }

    /** 필수 요청 파라미터 누락 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("MissingServletRequestParameterException: {}", e.getMessage());
        return error(ErrorResponseCode.INVALID_HTTP_MESSAGE_PARAMETER);
    }

    /** 멀티파트 요청 파트 누락 */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingPart(MissingServletRequestPartException e) {
        log.warn("MissingServletRequestPartException: {}", e.getMessage());
        return error(ErrorResponseCode.INVALID_HTTP_MESSAGE_PARAMETER);
    }

    /** 서버의 멀티파트 제한을 초과한 파일 업로드 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.warn("MaxUploadSizeExceededException: {}", e.getMessage());
        return error(OcrErrorCode.FILE_TOO_LARGE);
    }

    /** 지원하지 않는 HTTP 메서드 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("HttpRequestMethodNotSupportedException: {}", e.getMessage());
        return error(ErrorResponseCode.UNSUPPORTED_HTTP_METHOD);
    }

    /** 존재하지 않는 엔드포인트 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFound(NoHandlerFoundException e) {
        log.warn("NoHandlerFoundException: {}", e.getMessage());
        return error(ErrorResponseCode.NOT_FOUND_ENDPOINT);
    }

    /** 정적 리소스도 찾지 못함 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("NoResourceFoundException: {}", e.getMessage());
        return error(ErrorResponseCode.NOT_FOUND_ENDPOINT);
    }

    /** 그 외 처리되지 않은 모든 예외 — 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return error(ErrorResponseCode.SERVER_ERROR);
    }

    // ── helpers ─────────────────────────────────────────────

    private ResponseEntity<ApiResponse<Object>> error(BaseResponseCode code) {
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.error(code));
    }

    private ResponseEntity<ApiResponse<Object>> error(BaseResponseCode code, String message) {
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.error(code, message));
    }

    private ResponseEntity<ApiResponse<Object>> error(BaseResponseCode code, ErrorDetail detail) {
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.error(code, detail));
    }

    private List<FieldError> toFieldErrors(List<org.springframework.validation.FieldError> fieldErrors) {
        // 같은 필드에 검증 에러가 여러 개면 ", " 로 합쳐 필드당 한 항목으로 만든다.
        Map<String, String> merged = new LinkedHashMap<>();
        for (org.springframework.validation.FieldError fe : fieldErrors) {
            String reason = fe.getDefaultMessage();
            if (reason == null || reason.isBlank()) {
                continue; // 문구 없는 에러는 건너뛴다
            }
            merged.merge(fe.getField(), reason, (existing, next) -> existing + ", " + next);
        }
        return merged.entrySet().stream()
                .map(entry -> FieldError.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    /** HttpMessageNotReadableException 이 Enum 값 오류 때문인지 판별 */
    private boolean isEnumInvalidationError(HttpMessageNotReadableException e) {
        Throwable root = e.getRootCause();
        if (root == null || root.getMessage() == null) {
            return false;
        }
        String message = root.getMessage();
        return message.contains("Enum class") && message.contains("not one of the values accepted");
    }
}
