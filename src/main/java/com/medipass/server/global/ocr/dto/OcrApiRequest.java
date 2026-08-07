package com.medipass.server.global.ocr.dto;

import java.util.List;

public record OcrApiRequest(
        String version,
        String requestId,
        long timestamp,
        List<Image> images
) {
    public record Image(
            String format,
            String name
    ) {
    }
}
