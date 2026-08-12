package com.medipass.server.domain.medication.service;

import com.medipass.server.domain.medication.dto.response.MedicationScanResponse;
import com.medipass.server.domain.medication.dto.response.MedicationCandidateResponse;
import com.medipass.server.domain.medication.dto.response.ScannedMedication;
import com.medipass.server.global.ocr.client.OcrClient;
import com.medipass.server.global.ocr.dto.OcrTextResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MedicationScanService {

    private final OcrClient ocrClient;
    private final MedicationScanParser medicationScanParser;
    private final MedicationCandidateService medicationCandidateService;

    public MedicationScanResponse scan(MultipartFile file) {
        OcrTextResult ocrText = ocrClient.extract(file);
        MedicationScanResponse parsed = medicationScanParser.parse(ocrText);

        /*
         * 프론트가 OCR API와 식약처 매칭 API를 따로 호출하지 않도록
         * OCR에서 추출된 각 제품명을 백엔드에서 즉시 자동 매칭한다.
         * 식약처 결과가 여러 건이면 MedicationCandidateService의 MVP 정책에 따라
         * 첫 번째 제품이 선택되고, 결과가 없으면 matchedProduct는 null이 된다.
         */
        return new MedicationScanResponse(
                parsed.dispensedAt(),
                parsed.issuer(),
                parsed.medications().stream()
                        .map(this::matchMedication)
                        .toList()
        );
    }

    private ScannedMedication matchMedication(ScannedMedication medication) {
        MedicationCandidateResponse matchedProduct = medicationCandidateService
                .search(medication.ocrProductText())
                .matchedProduct();

        /*
         * 식약처에서 최초 조회한 제품 식별정보를 프론트가 최종 저장 요청에
         * 그대로 포함할 수 있도록 품목코드와 한글 제품명을 함께 반환한다.
         * 식약처 검색 결과가 없으면 두 공식 제품정보를 모두 null로 반환한다.
         * 프론트는 mfdsProductCode가 null인 항목에 인식 실패 및 재촬영 안내를 표시하고,
         * 해당 항목이 포함된 상태에서는 최종 저장을 진행하지 않는다.
         */
        return new ScannedMedication(
                medication.ocrProductText(),
                matchedProduct == null ? null : matchedProduct.mfdsProductCode(),
                matchedProduct == null ? null : matchedProduct.productKoName(),
                medication.intakesPerDay(),
                medication.totalDays(),
                medication.dosePerIntake(),
                medication.doseUnit()
        );
    }
}
