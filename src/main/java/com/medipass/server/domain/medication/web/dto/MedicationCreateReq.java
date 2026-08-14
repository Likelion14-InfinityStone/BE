package com.medipass.server.domain.medication.web.dto;

import com.medipass.server.domain.medication.entity.DoseUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MedicationCreateReq(
        @NotNull(message = "조제일자를 입력해 주세요.")
        LocalDate dispensedAt,

        @NotBlank(message = "발행기관은 필수입니다.")
        String issuer,

        @NotEmpty(message = "입력해 주세요")
        List<@Valid Item> medications
) {

    @Schema(name = "MedicationCreateItem")
    public record Item(
            @NotBlank(message = "식약처 품목코드는 필수입니다.")
            String mfdsProductCode,

            @NotBlank(message = "한글 제품명은 필수입니다.")
            String productKoName,

            @NotBlank(message = "영문 제품명은 필수입니다.")
            String productEnName,

            @NotNull(message = "복용 횟수를 입력해 주세요.")
            @Positive(message = "복용 횟수는 1 이상이어야 합니다.")
            Integer intakesPerDay,

            @NotNull(message = "복용 일수를 입력해 주세요.")
            @Positive(message = "복용 일수는 1 이상이어야 합니다.")
            Integer totalDays,

            @NotNull(message = "1회 복용량을 입력해 주세요.")
            @DecimalMin(value = "0.0", inclusive = false, message = "1회 복용량은 0보다 커야 합니다.")
            BigDecimal dosePerIntake,

            @NotNull(message = "복용 단위를 입력해 주세요.")
            DoseUnit doseUnit
    ) {
    }
}
