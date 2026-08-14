package com.medipass.server.domain.medication.service;

import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.domain.medication.entity.MfdsProduct;
import com.medipass.server.domain.medication.exception.MedicationDuplicateException;
import com.medipass.server.domain.medication.exception.MedicationProductMismatchException;
import com.medipass.server.domain.medication.repository.MedicationRepository;
import com.medipass.server.domain.medication.web.dto.MedicationCreateReq;
import com.medipass.server.domain.medication.web.dto.MedicationCreateRes;
import com.medipass.server.domain.medication.web.dto.MedicationCardPageRes;
import com.medipass.server.domain.medication.web.dto.MedicationListRes;
import com.medipass.server.domain.trip.entity.TripMedication;
import com.medipass.server.domain.trip.repository.TripMedicationRepository;
import com.medipass.server.domain.user.entity.User;
import com.medipass.server.domain.user.repository.UserRepository;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final TripMedicationRepository tripMedicationRepository;
    private final UserRepository userRepository;
    private final MfdsProductService mfdsProductService;

    // 사용자의 복약카드 목록을 최근 등록순으로 조회한다.
    @Transactional(readOnly = true)
    public MedicationListRes getAll(Long userId) {
        return MedicationListRes.from(
                medicationRepository.findByUser_IdOrderByCreatedAtDesc(userId)
        );
    }

    @Transactional(readOnly = true)
    public MedicationCardPageRes getCards(Long userId, int page, int size) {
        // 카드가 없어도 닉네임은 반환한다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(
                        ErrorResponseCode.NOT_FOUND_RESOURCE,
                        "사용자 정보를 찾을 수 없습니다."
                ));

        // 가로 스와이프용 카드를 최근 등록순으로 페이지 조회한다.
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );
        Page<Medication> medications = medicationRepository.findByUser_Id(userId, pageRequest);

        // 현재 페이지 카드들의 연결 여행을 한 번에 조회해 카드별로 묶는다.
        List<Long> medicationIds = medications.getContent().stream()
                .map(Medication::getId)
                .toList();
        Map<Long, List<TripMedication>> tripMedicationsByMedicationId = medicationIds.isEmpty()
                ? Map.of()
                : tripMedicationRepository
                        .findByMedication_IdInAndMedication_User_Id(medicationIds, userId)
                        .stream()
                        .collect(Collectors.groupingBy(item -> item.getMedication().getId()));

        // 닉네임, 카드 앞·뒷면 정보와 페이지 정보를 응답 DTO로 변환한다.
        return MedicationCardPageRes.from(
                user.getNickName(),
                medications,
                tripMedicationsByMedicationId
        );
    }

    @Transactional
    public MedicationCreateRes create(Long userId, MedicationCreateReq request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(
                        ErrorResponseCode.NOT_FOUND_RESOURCE,
                        "사용자 정보를 찾을 수 없습니다."
                ));

        validateNoDuplicates(userId, request);

        /*
         * 조제일과 발행기관은 한 장의 약 봉투에서 추출된 공통 정보이므로
         * 같은 요청에 포함된 모든 의약품에 동일하게 저장한다.
         * 복용 횟수·일수·1회 복용량은 OCR 결과 확인 화면에서 사용자가
         * 보정한 최종 값을 요청으로 받아 저장한다.
         */
        List<Medication> medications = request.medications().stream()
                .map(item -> toMedication(user, request, item))
                .toList();

        try {
            return MedicationCreateRes.from(medicationRepository.saveAllAndFlush(medications));
        } catch (DataIntegrityViolationException e) {
            // 사전 검사 이후 동시에 같은 약이 등록된 경우에도 동일한 409 응답을 보장한다.
            throw new MedicationDuplicateException();
        }
    }

    private void validateNoDuplicates(Long userId, MedicationCreateReq request) {
        Set<String> requestedProductCodes = new HashSet<>();

        for (MedicationCreateReq.Item item : request.medications()) {
            String productCode = item.mfdsProductCode().trim();
            boolean duplicatedInRequest = !requestedProductCodes.add(productCode);
            boolean alreadyRegistered = medicationRepository
                    .existsByUser_IdAndProduct_MfdsProductCode(userId, productCode);

            if (duplicatedInRequest || alreadyRegistered) {
                throw new MedicationDuplicateException();
            }
        }
    }

    private Medication toMedication(
            User user,
            MedicationCreateReq request,
            MedicationCreateReq.Item item
    ) {
        /*
         * 품목코드로 제품 마스터를 확보한다 (있으면 재사용, 없으면 MFDS 1회 조회해 저장).
         * 제품명·성분·함량은 마스터가 보유하므로 medication 에는 복용 정보와 참조만 남는다.
         */
        MfdsProduct product = mfdsProductService.getOrCreate(
                item.mfdsProductCode().trim(), item.productKoName().trim());
        validateProductIdentity(product, item);

        return Medication.create(
                user,
                product,
                request.dispensedAt(),
                trimToNull(request.issuer()),
                item.intakesPerDay(),
                item.totalDays(),
                item.dosePerIntake(),
                item.doseUnit()
        );
    }

    // 프론트에서 수정할 수 없는 스캔 제품명이 식약처 제품 마스터와 일치하는지 검증한다.
    private void validateProductIdentity(MfdsProduct product, MedicationCreateReq.Item item) {
        boolean koreanNameMismatch = !product.getProductKoName().equals(item.productKoName().trim());
        boolean englishNameMismatch = !product.getProductEnName().equals(item.productEnName().trim());

        if (koreanNameMismatch || englishNameMismatch) {
            throw new MedicationProductMismatchException();
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
