package com.medipass.server.domain.medication.service;

import com.medipass.server.domain.medication.web.dto.MedicationCandidateRecord;
import com.medipass.server.domain.medication.web.dto.MedicationScanRes;
import com.medipass.server.domain.medication.web.dto.ScannedMedicationRecord;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.ocr.client.OcrClient;
import com.medipass.server.global.ocr.dto.OcrTextResult;
import com.medipass.server.global.ocr.exception.OcrErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationScanService {

    private final OcrClient ocrClient;
    private final MedicationScanParser medicationScanParser;
    private final MedicationCandidateService medicationCandidateService;

    public MedicationScanRes scan(MultipartFile file) {
        OcrTextResult ocrText = ocrClient.extract(file);
        MedicationScanRes parsed = medicationScanParser.parse(ocrText);
        if (parsed.medications().isEmpty()) {
            log.warn("OCR 약품명 추출 실패: OCR 필드 수={}", ocrText.fields().size());
            throw new BaseException(OcrErrorCode.OCR_NOT_RECOGNIZED);
        }

        log.info("OCR 약품명 추출 완료: 약품 수={}, 제품명={}",
                parsed.medications().size(),
                parsed.medications().stream().map(ScannedMedicationRecord::ocrProductText).toList());

        /*
         * 프론트가 OCR API와 식약처 매칭 API를 따로 호출하지 않도록
         * OCR에서 추출된 각 제품명을 백엔드에서 즉시 자동 매칭한다.
         * 식약처 결과가 여러 건이면 스캔 흐름에서는 기존처럼 첫 번째 제품을 자동 선택한다.
         * 직접 입력 흐름에서는 후보 목록을 사용자에게 보여 주고 선택하도록 한다.
         */
        return new MedicationScanRes(
                parsed.dispensedAt(),
                parsed.issuer(),
                parsed.medications().stream()
                        .map(this::matchMedication)
                        .toList()
        );
    }

    private ScannedMedicationRecord matchMedication(ScannedMedicationRecord medication) {
        MedicationCandidateRecord matchedProduct = medicationCandidateService
                .search(medication.ocrProductText())
                .candidates().stream()
                .filter(this::hasRequiredProductInfo)
                .findFirst()
                .orElse(null);

        if (matchedProduct == null) {
            // 한 제품의 매칭 실패로 전체 스캔을 중단하지 않고 복용 정보를 남겨 직접 입력으로 연결한다.
            log.warn("식약처 의약품 매칭 실패: OCR 제품명={}", medication.ocrProductText());
            return medication;
        }

        log.info("식약처 의약품 매칭 완료: OCR 제품명={}, 품목코드={}",
                medication.ocrProductText(), matchedProduct.mfdsProductCode());

        /*
         * 식약처에서 최초 조회한 제품 식별정보를 프론트가 최종 저장 요청에
         * 그대로 포함할 수 있도록 품목코드와 한글·영문 제품명을 함께 반환한다.
         * 복용 단위를 추론하지 못한 경우에도 나머지 결과는 반환해 사용자가 직접 보정할 수 있게 한다.
         */
        return new ScannedMedicationRecord(
                medication.ocrProductText(),
                matchedProduct.mfdsProductCode(),
                matchedProduct.productKoName(),
                matchedProduct.productEnName(),
                medication.intakesPerDay(),
                medication.totalDays(),
                medication.dosePerIntake(),
                medication.doseUnit()
        );
    }

    private boolean hasRequiredProductInfo(MedicationCandidateRecord candidate) {
        return candidate.mfdsProductCode() != null
                && candidate.productKoName() != null
                && candidate.productEnName() != null;
    }
}
