package com.medipass.server.global.jwt.exception;

import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.response.code.ErrorResponseCode;

public class TokenExpiredException extends BaseException {
    public TokenExpiredException() { super(ErrorResponseCode.EXPIRED_TOKEN); }
}
