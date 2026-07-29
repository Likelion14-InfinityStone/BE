package com.medipass.server.global.response;

/**
 * 검증 실패 시 어떤 필드가 왜 틀렸는지를 담는 단위
 * */
public record FieldError(String field, String reason) {

    public static FieldError of(String field, String reason) {
        return new FieldError(field, reason);
    }
}
