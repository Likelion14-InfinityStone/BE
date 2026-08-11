package com.medipass.server.domain.medication.service;

import com.medipass.server.domain.medication.dto.response.MedicationScanResponse;
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

    public MedicationScanResponse scan(MultipartFile file) {
        OcrTextResult ocrText = ocrClient.extract(file);
        return medicationScanParser.parse(ocrText);
    }
}
