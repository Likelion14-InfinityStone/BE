package com.medipass.server.global.ocr.dto;

import java.util.List;

public record OcrResult(
        String rawText,
        List<String> lines
) {
}
