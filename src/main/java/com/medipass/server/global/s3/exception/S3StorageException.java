package com.medipass.server.global.s3.exception;

import com.medipass.server.global.exception.BaseException;

public class S3StorageException extends BaseException {

    public S3StorageException() {
        super(S3ErrorCode.STORAGE_ERROR);
    }
}
