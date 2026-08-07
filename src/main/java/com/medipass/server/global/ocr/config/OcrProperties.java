package com.medipass.server.global.ocr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ocr")
public record OcrProperties(
        String invokeUrl,
        String secretKey
) {
}
