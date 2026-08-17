package com.medipass.server.domain.sos.web.dto;

import com.medipass.server.domain.sos.entity.SosContactType;

/**
 * 결과 화면 연락처 한 줄
 * 채울 수 없는 줄은 아예 빼고 내려주므로 개수가 상황마다 다를 수 있다
 */
public record SosContact(
        int order,             // 화면 표시 순서 (1부터)
        SosContactType type,
        String name,           // 현지 경찰서 / 주 오사카 대한민국 총영사관 / 도쿄도보건의료정보센터
        String phone,          // 현지 단축번호(110) 또는 E.164(+81...)
        String note            // 주의 문구, 없으면 null
) {
}
