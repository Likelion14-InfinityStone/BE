package com.medipass.server.domain.sos.web.dto;

import com.medipass.server.domain.sos.entity.SosSituation;
import jakarta.validation.constraints.NotNull;

/**
 * 결과 화면 연락처 요청
 *
 * GET 이 아니라 POST 인 이유는 현위치 좌표 때문이다.
 * 쿼리스트링에 실으면 접근로그·프록시에 사용자의 위치가 그대로 남는다.
 */
public record SosContactReq(

        @NotNull(message = "상황을 선택해주세요")
        SosSituation situation, // 1번 줄이 경찰(110)이냐 응급(119)이냐를 가른다

        @NotNull(message = "여행을 선택해주세요")
        Long tripId,            // 도착 국가를 여기서 알아낸다

        // 공관 중 가장 가까운 곳을 고르는 데만 쓴다. 위치 권한을 거부하면 null (대사관으로 폴백)
        GeoPoint location
) {
}
