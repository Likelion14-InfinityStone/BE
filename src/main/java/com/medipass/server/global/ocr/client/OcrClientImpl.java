package com.medipass.server.global.ocr.client;

import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.ocr.config.OcrProperties;
import com.medipass.server.global.ocr.dto.OcrApiRequest;
import com.medipass.server.global.ocr.dto.OcrApiResponse;
import com.medipass.server.global.ocr.dto.OcrTextField;
import com.medipass.server.global.ocr.dto.OcrTextResult;
import com.medipass.server.global.ocr.exception.OcrErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class OcrClientImpl implements OcrClient {

    private static final String SECRET_HEADER = "X-OCR-SECRET";
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 업로드 가능한 최대 파일 크기: 10MB
    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of( // 허용하는 이미지 형식
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final OcrProperties properties;

    public OcrClientImpl(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            OcrProperties properties
    ) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public OcrTextResult extract(MultipartFile file) {
        validate(file);

        try {
            OcrApiResponse response = restClient.post()
                    .uri(properties.invokeUrl())
                    .header(SECRET_HEADER, properties.secretKey())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(createMultipartBody(file))
                    .retrieve()

                    // CLOVA 인증 실패
                    .onStatus(status -> status.value() == 401,
                            (request, responseData) -> {
                                throw new BaseException(OcrErrorCode.OCR_SERVICE_ERROR);
                            })
                    // 잘못된 요청 등 4xx 오류
                    .onStatus(HttpStatusCode::is4xxClientError,
                            (request, responseData) -> {
                                throw new BaseException(OcrErrorCode.OCR_SERVICE_ERROR);
                            })
                    // CLOVA 서버 오류
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, responseData) -> {
                                throw new BaseException(OcrErrorCode.OCR_SERVICE_ERROR);
                            })
                    .body(OcrApiResponse.class);

            return toResult(response);
        } catch (BaseException e) {
            throw e;
        } catch (RestClientException | JacksonException e) {
            throw new BaseException(OcrErrorCode.OCR_SERVICE_ERROR);
        }
    }

    private MultiValueMap<String, Object> createMultipartBody(MultipartFile file) throws JacksonException {
        // 이미지 형식에 맞춰 OCR 요청 정보 생성
        String format = MediaType.IMAGE_PNG_VALUE.equals(file.getContentType()) ? "png" : "jpg";
        OcrApiRequest request = new OcrApiRequest(
                "V2",
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                List.of(new OcrApiRequest.Image(format, "medication-bag"))
        );

        HttpHeaders messageHeaders = new HttpHeaders();
        messageHeaders.setContentType(MediaType.APPLICATION_JSON);

        String filename = file.getOriginalFilename() == null
                ? "medication-bag." + format
                : file.getOriginalFilename();
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
        fileHeaders.setContentDispositionFormData("file", filename);

        // 요청 정보와 이미지 파일을 multipart 본문에 추가
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("message", new HttpEntity<>(objectMapper.writeValueAsString(request), messageHeaders));
        body.add("file", new HttpEntity<>(file.getResource(), fileHeaders));
        return body;
    }

    private OcrTextResult toResult(OcrApiResponse response) {
        // OCR 응답이 없으면 인식 실패 처리
        if (response == null || response.images() == null || response.images().isEmpty()) {
            throw new BaseException(OcrErrorCode.OCR_NOT_RECOGNIZED);
        }

        OcrApiResponse.ImageResult image = response.images().getFirst();
        if ("FAILURE".equalsIgnoreCase(image.inferResult())) {
            log.warn("OCR 이미지 인식 실패: inferResult={}, message={}",
                    image.inferResult(), image.message());
            throw new BaseException(OcrErrorCode.OCR_NOT_RECOGNIZED);
        }
        if (!"SUCCESS".equalsIgnoreCase(image.inferResult())) {
            log.warn("OCR 이미지 처리 오류: inferResult={}, message={}",
                    image.inferResult(), image.message());
            throw new BaseException(OcrErrorCode.OCR_SERVICE_ERROR);
        }
        if (image.fields() == null || image.fields().isEmpty()) {
            throw new BaseException(OcrErrorCode.OCR_NOT_RECOGNIZED);
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        // CLOVA가 반환한 텍스트 조각을 한 줄씩 결합
        for (OcrApiResponse.Field field : image.fields()) {
            if (field.inferText() == null || field.inferText().isBlank()) {
                continue;
            }
            if (!currentLine.isEmpty()) {
                currentLine.append(' ');
            }
            currentLine.append(field.inferText().trim());

            if (Boolean.TRUE.equals(field.lineBreak())) {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
            }
        }

        // 마지막 필드에 줄바꿈 표시가 없는 경우 남은 텍스트 추가
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }
        if (lines.isEmpty()) {
            throw new BaseException(OcrErrorCode.OCR_NOT_RECOGNIZED);
        }

        // 구조화 시 표의 위치 관계를 활용할 수 있도록 각 텍스트의 좌표 정보 저장
        List<OcrTextField> textFields = image.fields().stream()
                .filter(field -> field.inferText() != null && !field.inferText().isBlank())
                .map(field -> new OcrTextField(
                        field.inferText().trim(),
                        coordinate(field, true),
                        coordinate(field, false)
                ))
                .toList();

        OcrApiResponse.ConvertedImageInfo imageInfo = image.convertedImageInfo();
        return new OcrTextResult(
                String.join("\n", lines),
                List.copyOf(lines),
                textFields,
                imageInfo == null ? null : imageInfo.width(),
                imageInfo == null ? null : imageInfo.height()
        );
    }

    // 텍스트 영역의 첫 번째 꼭짓점에서 x 또는 y 좌표 추출
    private double coordinate(OcrApiResponse.Field field, boolean xAxis) {
        if (field.boundingPoly() == null
                || field.boundingPoly().vertices() == null
                || field.boundingPoly().vertices().isEmpty()) {
            return 0;
        }
        OcrApiResponse.Vertex vertex = field.boundingPoly().vertices().getFirst();
        Double coordinate = xAxis ? vertex.x() : vertex.y();
        return coordinate == null ? 0 : coordinate;
    }

    // 빈 파일, 용량 초과, 지원하지 않는 형식을 검증
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(OcrErrorCode.EMPTY_FILE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BaseException(OcrErrorCode.FILE_TOO_LARGE);
        }
        if (!SUPPORTED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BaseException(OcrErrorCode.UNSUPPORTED_FILE_TYPE);
        }
    }
}
