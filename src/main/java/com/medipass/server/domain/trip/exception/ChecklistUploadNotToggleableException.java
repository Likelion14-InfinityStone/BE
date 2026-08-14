package com.medipass.server.domain.trip.exception;

import com.medipass.server.global.exception.BaseException;

/**
 * 업로드(UPLOAD) 서류를 체크 토글로 바꾸려 할 때 (400)
 * 업로드 서류는 파일 업로드로만 완료 처리되고, 토글은 신청·문의(ACTION)만 대상이다
 */
public class ChecklistUploadNotToggleableException extends BaseException {

    public ChecklistUploadNotToggleableException() {
        super(TripErrorCode.CHECKLIST_UPLOAD_NOT_TOGGLEABLE);
    }
}
