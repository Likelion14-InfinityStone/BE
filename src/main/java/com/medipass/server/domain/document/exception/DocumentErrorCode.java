package com.medipass.server.domain.document.exception;

import com.medipass.server.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DocumentErrorCode implements BaseResponseCode {
    INVALID_FILE(HttpStatus.BAD_REQUEST, "DOCUMENT_400_1", "유효한 PDF 파일을 업로드해 주세요."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "DOCUMENT_400_2", "서류 파일은 10MB 이하여야 합니다."),
    ALREADY_EXISTS(HttpStatus.CONFLICT, "DOCUMENT_409_1", "이미 서류가 등록된 체크리스트 항목입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
