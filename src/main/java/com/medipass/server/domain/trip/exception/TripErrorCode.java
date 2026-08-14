package com.medipass.server.domain.trip.exception;

import com.medipass.server.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 여행 도메인 에러 코드 — BaseException 과 함께 던져 사용
 */
@Getter
@AllArgsConstructor
public enum TripErrorCode implements BaseResponseCode {

    TRIP_ACCESS_DENIED("TRIP_403_1", HttpStatus.FORBIDDEN, "본인 여행만 접근할 수 있습니다."),
    TRIP_NOT_FOUND("TRIP_404_1", HttpStatus.NOT_FOUND, "여행을 찾을 수 없습니다."),
    TRIP_MEDICATION_NOT_FOUND("TRIP_404_2", HttpStatus.NOT_FOUND, "여행에 담긴 약을 찾을 수 없습니다."),
    TRIP_TITLE_DUPLICATE("TRIP_409_1", HttpStatus.CONFLICT, "이미 같은 이름의 여행이 있습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
