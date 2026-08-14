package com.medipass.server.domain.medication.service;

import com.medipass.server.domain.medication.web.dto.MedicationCandidateRecord;
import com.medipass.server.domain.medication.web.dto.MedicationScanRes;
import com.medipass.server.domain.medication.web.dto.ScannedMedicationRecord;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.ocr.client.OcrClient;
import com.medipass.server.global.ocr.dto.OcrTextResult;
import com.medipass.server.global.ocr.exception.OcrErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MedicationScanService {

    private final OcrClient ocrClient;
    private final MedicationScanParser medicationScanParser;
    private final MedicationCandidateService medicationCandidateService;

    public MedicationScanRes scan(MultipartFile file) {
        OcrTextResult ocrText = ocrClient.extract(file);
        MedicationScanRes parsed = medicationScanParser.parse(ocrText);
        if (parsed.medications().isEmpty()) {
            throw new BaseException(OcrErrorCode.OCR_NOT_RECOGNIZED);
        }

        /*
         * 프론트가 OCR API와 식약처 매칭 API를 따로 호출하지 않도록
         * OCR에서 추출된 각 제품명을 백엔드에서 즉시 자동 매칭한다.
         * 식약처 결과가 여러 건이면 MedicationCandidateService의 MVP 정책에 따라
         * 첫 번째 제품이 선택되고, 결과가 없으면 matchedProduct는 null이 된다.
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
                .matchedProduct();

        if (matchedProduct == null
                || matchedProduct.mfdsProductCode() == null
                || matchedProduct.productKoName() == null
                || matchedProduct.productEnName() == null
                || medication.doseUnit() == null) {
            throw new BaseException(OcrErrorCode.OCR_NOT_RECOGNIZED);
        }

        /*
         * 식약처에서 최초 조회한 제품 식별정보를 프론트가 최종 저장 요청에
         * 그대로 포함할 수 있도록 품목코드와 한글·영문 제품명을 함께 반환한다.
         * 식약처 제품정보 또는 OCR에서 추론한 복용 단위가 없으면 사용자가 수정할 수 없으므로
         * 성공 응답 대신 OCR_422를 반환해 재촬영을 안내한다.
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
}
