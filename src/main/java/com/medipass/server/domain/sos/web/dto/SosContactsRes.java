package com.medipass.server.domain.sos.web.dto;

import java.util.List;

/**
 * 결과 화면 연락처 목록
 * 채울 수 없는 줄은 빠지므로 개수가 고정이 아니다 (일본이 아니면 재외공관만 남는다)
 */
public record SosContactsRes(
        List<SosContact> contacts
) {
}
