package com.medipass.server.global.ocr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OcrApiResponse(
        List<ImageResult> images
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageResult(
            String inferResult,
            String message,
            List<Field> fields
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Field(
            String inferText,
            Double inferConfidence,
            Boolean lineBreak
    ) {
    }
}
