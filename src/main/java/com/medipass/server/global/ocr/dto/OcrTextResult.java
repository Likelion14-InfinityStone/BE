package com.medipass.server.global.ocr.dto;

import java.util.List;

public record OcrTextResult(
        String rawText,
        List<String> lines,
        List<OcrTextField> fields,
        Integer imageWidth,
        Integer imageHeight
) {
    public OcrTextResult(String rawText, List<String> lines) {
        this(rawText, lines, List.of(), null, null);
    }
}
