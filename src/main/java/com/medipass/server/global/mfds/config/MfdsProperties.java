package com.medipass.server.global.mfds.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mfds")
public record MfdsProperties(
        String baseUrl,
        String serviceKey
) {
}
