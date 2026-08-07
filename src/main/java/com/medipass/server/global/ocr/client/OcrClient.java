package com.medipass.server.global.ocr.client;

import com.medipass.server.global.ocr.dto.OcrResult;
import org.springframework.web.multipart.MultipartFile;

public interface OcrClient {
    OcrResult extract(MultipartFile file);
}
