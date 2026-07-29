package com.medipass.server.global.response.code;

import org.springframework.http.HttpStatus;

public interface BaseResponseCode {

    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}
