package com.medipass.server.domain.medication.service;

import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.domain.medication.repository.MedicationRepository;
import com.medipass.server.domain.medication.web.dto.MedicationCreateReq;
import com.medipass.server.domain.medication.web.dto.MedicationCreateRes;
import com.medipass.server.domain.user.entity.User;
import com.medipass.server.domain.user.repository.UserRepository;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;

    @Transactional
    public MedicationCreateRes create(Long userId, MedicationCreateReq request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(
                        ErrorResponseCode.NOT_FOUND_RESOURCE,
                        "사용자 정보를 찾을 수 없습니다."
                ));

        /*
         * 조제일과 발행기관은 한 장의 약 봉투에서 추출된 공통 정보이므로
         * 같은 요청에 포함된 모든 의약품에 동일하게 저장한다.
         * 복용 횟수·일수·1회 복용량은 OCR 결과 확인 화면에서 사용자가
         * 보정한 최종 값을 요청으로 받아 저장한다.
         */
        List<Medication> medications = request.medications().stream()
                .map(item -> toMedication(user, request, item))
                .toList();

        return MedicationCreateRes.from(medicationRepository.saveAll(medications));
    }

    private Medication toMedication(
            User user,
            MedicationCreateReq request,
            MedicationCreateReq.Item item
    ) {
        /*
         * 식약처 API의 중복 호출을 피하기 위해 스캔 단계에서 조회해 프론트에
         * 반환했던 품목코드와 한글 제품명을 다시 받아 저장한다.
         */
        return Medication.create(
                user,
                item.mfdsProductCode().trim(),
                item.productKoName().trim(),
                request.dispensedAt(),
                trimToNull(request.issuer()),
                item.intakesPerDay(),
                item.totalDays(),
                item.dosePerIntake(),
                item.doseUnit()
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
