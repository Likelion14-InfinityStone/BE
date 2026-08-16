package com.medipass.server.global.s3.service;

import com.medipass.server.global.s3.config.S3Properties;
import com.medipass.server.global.s3.exception.S3StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private static final String DOCUMENT_PREFIX = "documents/";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    // 검증된 파일을 UUID 기반 객체 키로 저장한다.
    public UploadResult upload(MultipartFile file, String extension) {
        String objectKey = DOCUMENT_PREFIX
                + UUID.randomUUID()
                + "."
                + normalizeExtension(extension);
        String contentType = resolveContentType(file.getContentType());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );
            return new UploadResult(objectKey, file.getSize(), contentType);
        } catch (IOException | SdkException e) {
            log.error("S3 파일 업로드에 실패했습니다. 버킷={}, 객체 키={}", properties.bucket(), objectKey, e);
            throw new S3StorageException();
        }
    }

    // 원본 파일명으로 내려받을 수 있는 임시 URL을 생성한다.
    public URI createDownloadUrl(String objectKey, String originalFilename) {
        validateDocumentKey(objectKey);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .responseContentDisposition(contentDisposition(originalFilename))
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(properties.presignedUrlExpiration())
                .getObjectRequest(getObjectRequest)
                .build();

        try {
            return URI.create(s3Presigner.presignGetObject(presignRequest).url().toString());
        } catch (SdkException e) {
            log.error("S3 임시 다운로드 URL 생성에 실패했습니다. 버킷={}, 객체 키={}",
                    properties.bucket(), objectKey, e);
            throw new S3StorageException();
        }
    }

    // documents 경로에 저장된 객체를 삭제한다.
    public void delete(String objectKey) {
        validateDocumentKey(objectKey);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build();

        try {
            s3Client.deleteObject(request);
        } catch (SdkException e) {
            log.error("S3 파일 삭제에 실패했습니다. 버킷={}, 객체 키={}", properties.bucket(), objectKey, e);
            throw new S3StorageException();
        }
    }

    private String resolveContentType(String contentType) {
        return contentType == null || contentType.isBlank()
                ? DEFAULT_CONTENT_TYPE
                : contentType;
    }

    private String normalizeExtension(String extension) {
        if (extension == null || !extension.matches("[A-Za-z0-9]+")) {
            throw new IllegalArgumentException("파일 확장자 형식이 올바르지 않습니다.");
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    private String contentDisposition(String originalFilename) {
        String filename = originalFilename == null || originalFilename.isBlank()
                ? "document"
                : originalFilename.replace("\r", "").replace("\n", "");
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return "attachment; filename*=UTF-8''" + encodedFilename;
    }

    private void validateDocumentKey(String objectKey) {
        if (objectKey == null || !objectKey.startsWith(DOCUMENT_PREFIX)) {
            throw new IllegalArgumentException("서류 객체 키가 올바르지 않습니다.");
        }
    }

    public record UploadResult(
            String objectKey,
            long size,
            String contentType
    ) {
    }
}
