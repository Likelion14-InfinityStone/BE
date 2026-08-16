package com.medipass.server.global.s3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

// S3 버킷과 Presigned URL 관련 설정
@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
        String bucket,
        String region,
        Duration presignedUrlExpiration
) {
}
