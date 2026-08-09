package com.medipass.server.global.ocr.client;

import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.ocr.config.OcrProperties;
import com.medipass.server.global.ocr.dto.OcrResult;
import com.medipass.server.global.ocr.exception.OcrErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OcrClientImplTest {

    private static final String INVOKE_URL = "https://example.com/ocr";
    private static final String SECRET_KEY = "secret";

    private MockRestServiceServer server;
    private OcrClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OcrClientImpl(
                builder,
                new ObjectMapper(),
                new OcrProperties(INVOKE_URL, SECRET_KEY)
        );
    }

    @Test
    void extractsTextByLine() {
        String response = """
                {
                  "images": [{
                    "inferResult": "SUCCESS",
                    "message": "SUCCESS",
                    "fields": [
                      {"inferText": "콘서타", "inferConfidence": 0.99, "lineBreak": false},
                      {"inferText": "27mg", "inferConfidence": 0.98, "lineBreak": true},
                      {"inferText": "1일", "inferConfidence": 0.97, "lineBreak": false},
                      {"inferText": "1회", "inferConfidence": 0.96, "lineBreak": true}
                    ]
                  }]
                }
                """;
        server.expect(requestTo(INVOKE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-OCR-SECRET", SECRET_KEY))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        MockMultipartFile file = new MockMultipartFile(
                "file", "medication.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes()
        );

        OcrResult result = client.extract(file);

        assertThat(result.rawText()).isEqualTo("콘서타 27mg\n1일 1회");
        assertThat(result.lines()).containsExactly("콘서타 27mg", "1일 1회");
        server.verify();
    }

    @Test
    void rejectsUnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "medication.heic", "image/heic", "image".getBytes()
        );

        assertThatThrownBy(() -> client.extract(file))
                .isInstanceOfSatisfying(BaseException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(OcrErrorCode.UNSUPPORTED_FILE_TYPE));
    }
}
