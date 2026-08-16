package com.medipass.server.domain.document.exception;

import com.medipass.server.global.exception.BaseException;

public class DocumentException extends BaseException {

    public DocumentException(DocumentErrorCode errorCode) {
        super(errorCode);
    }
}
