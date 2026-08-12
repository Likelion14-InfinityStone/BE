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
         * 프론트에는 후속 약 등록에 필요한 품목코드와 화면 표시용 한글 제품명만 노출한다.
         * 그 외 식약처 상세정보는 MfdsClient 내부 응답에 유지하여
         * 추후 여행 도메인에서 필요할 때 재사용한다.
         * 식약처 검색 결과가 없으면 사용자가 인식 결과 화면에서 수정할 수 있도록
         * 한글 제품명에 OCR 원문을 대신 담는다.
         */
        return new ScannedMedication(
                medication.ocrProductText(),
                matchedProduct == null ? null : matchedProduct.mfdsProductCode(),
                matchedProduct == null ? medication.ocrProductText() : matchedProduct.productKoName(),
                medication.intakesPerDay(),
                medication.totalDays(),
                medication.dosePerIntake(),
                medication.doseUnit()
        );
    }
}
