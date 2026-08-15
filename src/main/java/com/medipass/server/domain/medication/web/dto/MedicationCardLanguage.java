package com.medipass.server.domain.medication.web.dto;

import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.response.code.ErrorResponseCode;

public enum MedicationCardLanguage {
    KO,
    EN;

    public static MedicationCardLanguage from(String value) {
        if (value == null || value.isBlank()) {
            return KO;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BaseException(
                    ErrorResponseCode.INVALID_HTTP_MESSAGE_PARAMETER,
                    "lang은 ko 또는 en만 사용할 수 있습니다."
            );
        }
    }
}
