package com.medipass.server.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

/**
 * 실패 응답의 result 에 담기는 에러 상세 — 필드별 에러 목록
 * */
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorDetail {

    private final List<FieldError> errors;

    private ErrorDetail(List<FieldError> errors) {
        this.errors = errors;
    }

    public static ErrorDetail of(List<FieldError> errors) {
        return new ErrorDetail(errors);
    }
}
