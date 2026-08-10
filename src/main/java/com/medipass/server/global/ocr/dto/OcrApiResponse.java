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
            ConvertedImageInfo convertedImageInfo,
            List<Field> fields
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConvertedImageInfo(
            Integer width,
            Integer height
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Field(
            String inferText,
            Double inferConfidence,
            Boolean lineBreak,
            BoundingPoly boundingPoly
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BoundingPoly(
            List<Vertex> vertices
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Vertex(
            Double x,
            Double y
    ) {
    }
}
