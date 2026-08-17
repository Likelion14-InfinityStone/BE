package com.medipass.server.global.mofa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mofa")
public record MofaProperties(
        String baseUrl,
        String serviceKey
) {
}
