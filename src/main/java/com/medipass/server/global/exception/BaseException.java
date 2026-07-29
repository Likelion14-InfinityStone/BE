package com.medipass.server.global.exception;

import com.medipass.server.global.response.code.BaseResponseCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final transient BaseResponseCode errorCode;

    public BaseException(BaseResponseCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BaseException(BaseResponseCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
