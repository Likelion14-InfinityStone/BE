package com.medipass.server.global.ocr.client;

import com.medipass.server.global.ocr.dto.OcrTextResult;
import org.springframework.web.multipart.MultipartFile;

public interface OcrClient {
    OcrTextResult extract(MultipartFile file);
}
