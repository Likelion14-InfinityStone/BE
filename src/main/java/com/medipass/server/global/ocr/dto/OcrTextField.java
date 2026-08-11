package com.medipass.server.global.ocr.dto;

public record OcrTextField(
        String text,
        double x,
        double y
) {
}
