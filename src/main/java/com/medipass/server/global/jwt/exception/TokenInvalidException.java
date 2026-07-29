package com.medipass.server.global.jwt.exception;

import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.response.code.ErrorResponseCode;

public class TokenInvalidException extends BaseException {
    public TokenInvalidException() {
        super(ErrorResponseCode.INVALID_TOKEN);
    }
}
