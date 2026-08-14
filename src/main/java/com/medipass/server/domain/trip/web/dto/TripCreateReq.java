package com.medipass.server.domain.trip.web.dto;

import com.medipass.server.domain.regulation.entity.PreparationLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

/**
 * 여행 생성 요청 — analyze 로 받은 판정 결과를 그대로 실어 저장 (재판정 안 함)
 * 출발/도착 공항 정보 + 날짜 + 체크한 약들(신호등·필요서류 포함)
 * title 미입력 시 "{도착국} 여행N" 으로 자동 생성
 */
public record TripCreateReq(

        String title, // 선택 (없으면 자동 생성)

        @NotNull(message = "출발지는 필수입니다")
        @Valid
        Airport origin,

        @NotNull(message = "도착지는 필수입니다")
        @Valid
        Airport destination,

        @NotNull(message = "출국일은 필수입니다")
        LocalDate departOn,

        @NotNull(message = "귀국일은 필수입니다")
        LocalDate returnOn,

        @NotEmpty(message = "챙길 약을 1개 이상 선택해야 합니다")
        @Valid
        List<MedicationItem> medications
) {

    // 공항 정보 (출발/도착 공통)
    public record Airport(

            @NotBlank(message = "공항 코드는 필수입니다")
            String airportCode,   // ICN

            @NotBlank(message = "도시는 필수입니다")
            String city,          // 인천

            @NotBlank(message = "국가 코드는 필수입니다")
            String codeAlpha3     // KOR (ISO 3166-1 alpha-3)
    ) {
    }

    // 챙길 약 한 건 (복약카드 id + 일수 + analyze 판정 결과)
    public record MedicationItem(

            @NotNull(message = "복약카드 id는 필수입니다")
            Long medicationId,

            @NotNull(message = "일수는 필수입니다")
            @Positive(message = "일수는 1 이상이어야 합니다")
            Integer carryDays,

            @NotNull(message = "판정 결과(신호등)는 필수입니다")
            PreparationLevel preparationLevel,   // analyze 응답의 preparationLevel

            @NotNull(message = "통제 성분 포함 여부는 필수입니다")
            Boolean regulated,                   // analyze 응답 (통제 성분 포함 여부)

            String categoryCode,                 // analyze 응답 (규제 없으면 null)
            String categoryName,                 // analyze 응답 (규제 없으면 null)
            String quantityCondition,            // analyze 응답 (규제 없으면 null)

            List<Long> requirementTemplateIds    // analyze 응답의 서류 templateId 들 (없으면 빈 배열)
    ) {
    }
}
