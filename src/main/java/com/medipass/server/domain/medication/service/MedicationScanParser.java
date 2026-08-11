package com.medipass.server.domain.medication.service;

import com.medipass.server.domain.medication.dto.response.MedicationScanResponse;
import com.medipass.server.domain.medication.dto.response.ScannedMedication;
import com.medipass.server.global.ocr.dto.OcrTextField;
import com.medipass.server.global.ocr.dto.OcrTextResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MedicationScanParser {

    private static final double DEFAULT_ROW_Y_TOLERANCE = 12.0;
    private static final double MIN_ROW_Y_TOLERANCE = 6.0;
    private static final double ROW_Y_TOLERANCE_RATIO = 0.004;
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(20\\d{2})\\s*[년./-]\\s*(\\d{1,2})\\s*[월./-]\\s*(\\d{1,2})\\s*일?"
    );

    public MedicationScanResponse parse(OcrTextResult ocrText) {
        // OCR 좌표를 행 단위로 묶은 뒤 공통 정보와 약 정보를 추출
        List<Row> rows = groupRows(ocrText.fields(), rowYTolerance(ocrText.imageHeight()));
        LocalDate dispensedAt = findDispensedAt(rows, ocrText.rawText());
        String issuer = findIssuer(rows);

        List<ScannedMedication> medications = findMedications(rows, ocrText.imageWidth());

        return new MedicationScanResponse(dispensedAt, issuer, medications);
    }

    private List<Row> groupRows(List<OcrTextField> fields, double rowYTolerance) {
        if (fields == null || fields.isEmpty()) {
            return List.of();
        }

        // 위에서 아래, 같은 행에서는 왼쪽에서 오른쪽 순서로 정렬
        List<OcrTextField> sorted = fields.stream()
                .sorted(Comparator.comparingDouble(OcrTextField::y).thenComparingDouble(OcrTextField::x))
                .toList();
        List<List<OcrTextField>> grouped = new ArrayList<>();

        for (OcrTextField field : sorted) {
            List<OcrTextField> row = grouped.stream()
                    .filter(candidate -> Math.abs(averageY(candidate) - field.y()) <= rowYTolerance)
                    .findFirst()
                    .orElse(null);
            if (row == null) {
                row = new ArrayList<>();
                grouped.add(row);
            }
            row.add(field);
        }

        return grouped.stream()
                .map(row -> row.stream().sorted(Comparator.comparingDouble(OcrTextField::x)).toList())
                .map(Row::new)
                .sorted(Comparator.comparingDouble(Row::y))
                .toList();
    }

    private LocalDate findDispensedAt(List<Row> rows, String fallbackText) {
        // 조제일자 라벨이 있는 행의 날짜를 우선 사용
        for (Row row : rows) {
            if (compact(row.text()).contains("조제일자")) {
                LocalDate date = parseDate(row.text());
                if (date != null) {
                    return date;
                }
            }
        }
        return parseDate(fallbackText);
    }

    private String findIssuer(List<Row> rows) {
        // 처방전 발행기관 또는 병원정보 라벨 뒤에서 병원명 추출
        for (Row row : rows) {
            List<OcrTextField> fields = row.fields();
            for (int index = 0; index < fields.size(); index++) {
                if (!fields.get(index).text().contains("발행기관")) {
                    continue;
                }
                for (int valueIndex = index + 1; valueIndex < fields.size(); valueIndex++) {
                    String value = fields.get(valueIndex).text().replaceFirst("^[\\s:：·]+", "").trim();
                    if (!value.isBlank()) {
                        return value;
                    }
                }
            }

            String rowText = row.text();
            int labelIndex = rowText.indexOf("병원정보");
            if (labelIndex < 0) {
                continue;
            }
            String value = rowText.substring(labelIndex + "병원정보".length())
                    .replaceFirst("^[\\s.:：·]+", "")
                    .replaceFirst("(?i)Tel[.:].*$", "")
                    .replaceFirst("\\s*\\[.*$", "")
                    .replaceFirst("\\s*\\(.*$", "")
                    .trim();
            return value.isBlank() ? null : value;
        }
        return null;
    }

    private List<ScannedMedication> findMedications(List<Row> rows, Integer actualImageWidth) {
        double imageWidth = effectiveImageWidth(rows, actualImageWidth);

        // 약품사진 영역에 표시된 약만 등록 대상으로 사용
        List<OcrTextField> photoMedications = findPhotoMedications(rows, imageWidth);
        if (photoMedications.isEmpty()) {
            return List.of();
        }

        // 영수증 왼쪽 하단에서 약 이름과 투약량/횟수/일수 행 추출
        List<Row> receiptRows = rows.stream()
                .filter(row -> row.fields().stream().anyMatch(field -> isReceiptMedicationField(field, imageWidth)))
                .toList();
        if (receiptRows.isEmpty()) {
            return List.of();
        }

        List<ReceiptDosage> dosages = receiptRows.stream()
                .map(row -> {
                    OcrTextField receiptProductField = row.fields().stream()
                            .filter(field -> isReceiptMedicationField(field, imageWidth))
                            .findFirst()
                            .orElseThrow();
                    List<OcrTextField> numbers = row.fields().stream()
                            .filter(field -> field.x() > receiptProductField.x())
                            .filter(field -> field.text().matches("\\d+(?:\\.\\d+)?"))
                            .sorted(Comparator.comparingDouble(OcrTextField::x))
                            .toList();
                    return new ReceiptDosage(
                            receiptProductField.text(),
                            numbers.isEmpty() ? null : new BigDecimal(numbers.get(0).text()),
                            numbers.size() < 2 ? null : decimalToInteger(numbers.get(1).text()),
                            numbers.size() < 3 ? null : decimalToInteger(numbers.get(2).text())
                    );
                })
                .toList();

        // 사진 영역과 영수증 영역의 약 이름을 비교해 복용 정보 연결
        List<ScannedMedication> medications = new ArrayList<>();
        List<ReceiptDosage> unusedDosages = new ArrayList<>(dosages);
        for (OcrTextField productField : photoMedications) {
            ReceiptDosage matchedDosage = findBestDosage(productField.text(), unusedDosages);
            if (matchedDosage != null) {
                unusedDosages.remove(matchedDosage);
            }
            ReceiptDosage dosage = matchedDosage == null ? ReceiptDosage.empty() : matchedDosage;
            medications.add(new ScannedMedication(
                    productField.text(),
                    dosage.intakesPerDay(),
                    dosage.totalDays(),
                    dosage.dosePerIntake(),
                    inferDoseUnit(productField.text())
            ));
        }
        return List.copyOf(medications);
    }

    private ReceiptDosage findBestDosage(String photoProduct, List<ReceiptDosage> candidates) {
        // OCR 오타와 말줄임표를 고려해 가장 긴 공통 문자열로 약 이름 비교
        ReceiptDosage best = null;
        int bestScore = 0;
        for (ReceiptDosage candidate : candidates) {
            int score = longestCommonSubstring(
                    normalizeProductText(photoProduct),
                    normalizeProductText(candidate.ocrProductText())
            );
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return bestScore >= 3 ? best : null;
    }

    private String normalizeProductText(String text) {
        return text == null ? "" : text.replaceAll("[^가-힣A-Za-z0-9]", "");
    }

    private int longestCommonSubstring(String left, String right) {
        int longest = 0;
        int[][] lengths = new int[left.length() + 1][right.length() + 1];
        for (int leftIndex = 1; leftIndex <= left.length(); leftIndex++) {
            for (int rightIndex = 1; rightIndex <= right.length(); rightIndex++) {
                if (left.charAt(leftIndex - 1) == right.charAt(rightIndex - 1)) {
                    lengths[leftIndex][rightIndex] = lengths[leftIndex - 1][rightIndex - 1] + 1;
                    longest = Math.max(longest, lengths[leftIndex][rightIndex]);
                }
            }
        }
        return longest;
    }

    private List<OcrTextField> findPhotoMedications(List<Row> rows, double imageWidth) {
        List<OcrTextField> fields = rows.stream().flatMap(row -> row.fields().stream()).toList();
        boolean hasPhotoHeader = fields.stream()
                .anyMatch(field -> field.text().contains("약품사진"));
        if (!hasPhotoHeader) {
            return List.of();
        }

        // 이미지 너비 비율로 약품명 열의 범위 결정
        return fields.stream()
                .filter(field -> field.x() >= imageWidth * 0.25 && field.x() < imageWidth * 0.45)
                .filter(this::isPhotoMedicationField)
                .sorted(Comparator.comparingDouble(OcrTextField::y))
                .toList();
    }

    private boolean isPhotoMedicationField(OcrTextField field) {
        String text = field.text();
        return text.length() >= 5
                && (text.contains("정") || text.contains("캡슐") || text.contains("서방"))
                && !text.startsWith("[")
                && !text.contains("정보")
                && !text.contains("병원")
                && !text.contains("환자")
                && !text.contains("조제")
                && !text.contains("약품사진")
                && !text.contains("주의사항")
                && !text.contains("보관")
                && !text.contains("정제")
                && !text.contains("코팅")
                && !text.contains("원형")
                && !text.contains("백색")
                && !text.contains("황색")
                && !text.contains("경질");
    }

    private boolean isReceiptMedicationField(OcrTextField field, double imageWidth) {
        if (field.x() >= imageWidth * 0.18) {
            return false;
        }
        String text = field.text();
        return text.length() >= 5
                && (text.contains("정") || text.contains("캡슐") || text.contains("크림")
                || text.contains("서방") || text.contains("독시"))
                && !text.contains("영수증")
                && !text.contains("조제")
                && !text.contains("복용");
    }

    private double effectiveImageWidth(List<Row> rows, Integer actualImageWidth) {
        // 실제 이미지 너비가 없을 때 가장 오른쪽 OCR 좌표를 대체값으로 사용
        if (actualImageWidth != null && actualImageWidth > 0) {
            return actualImageWidth;
        }
        return rows.stream()
                .flatMap(row -> row.fields().stream())
                .mapToDouble(OcrTextField::x)
                .max()
                .orElse(0);
    }

    private double rowYTolerance(Integer imageHeight) {
        // 해상도에 따라 같은 행으로 판단할 세로 오차 범위 계산
        if (imageHeight == null || imageHeight <= 0) {
            return DEFAULT_ROW_Y_TOLERANCE;
        }
        return Math.max(MIN_ROW_Y_TOLERANCE, imageHeight * ROW_Y_TOLERANCE_RATIO);
    }

    private Integer decimalToInteger(String value) {
        try {
            return new BigDecimal(value).intValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            return null;
        }
    }

    private String inferDoseUnit(String product) {
        // OCR 제품명의 제형 표현을 API enum 값으로 변환
        if (product.contains("캡슐")) {
            return "CAPSULE";
        }
        if (product.contains("정") || product.contains("서방")) {
            return "TABLET";
        }
        return null;
    }

    private LocalDate parseDate(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            return LocalDate.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))
            );
        } catch (DateTimeException e) {
            return null;
        }
    }

    private String compact(String text) {
        return text == null ? "" : text.replaceAll("\\s+", "");
    }

    private double averageY(List<OcrTextField> fields) {
        return fields.stream().mapToDouble(OcrTextField::y).average().orElse(0);
    }

    private record Row(List<OcrTextField> fields) {
        private String text() {
            return fields.stream().map(OcrTextField::text).reduce((left, right) -> left + " " + right).orElse("");
        }

        private double y() {
            return fields.stream().mapToDouble(OcrTextField::y).average().orElse(0);
        }
    }

    private record ReceiptDosage(
            String ocrProductText,
            BigDecimal dosePerIntake,
            Integer intakesPerDay,
            Integer totalDays
    ) {
        private static ReceiptDosage empty() {
            return new ReceiptDosage("", null, null, null);
        }
    }
}
