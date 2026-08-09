package com.medipass.server.domain.medication.service;

import com.medipass.server.global.ocr.client.OcrClient;
import com.medipass.server.global.ocr.dto.OcrResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MedicationScanService {

    private final OcrClient ocrClient;

    public OcrResult extract(MultipartFile file) {
        return ocrClient.extract(file);
    }
}
