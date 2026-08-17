package com.medipass.server.domain.sos.service;

import com.medipass.server.domain.sos.web.dto.SosContact;
import com.medipass.server.domain.sos.web.dto.SosContactReq;
import com.medipass.server.domain.sos.web.dto.SosContactsRes;
import com.medipass.server.domain.trip.entity.Trip;
import com.medipass.server.domain.trip.exception.TripAccessDeniedException;
import com.medipass.server.domain.trip.exception.TripNotFoundException;
import com.medipass.server.domain.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SOS 긴급 도움
 *
 * 대응 순서·안내 문구·버튼 라벨·상황 4종은 상황별 고정 문구라 프론트가 들고 있고,
 * 서버는 국가마다 달라지는 연락처만 만든다. 저장하지 않는다 (이력 화면 없음).
 * 현지어 설명문은 별도 API 로 분리되어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SosService {

    private final TripRepository tripRepository;
    private final SosContactAssembler contactAssembler;

    public SosContactsRes getContacts(Long userId, SosContactReq request) {
        Trip trip = loadOwnedTrip(userId, request.tripId());

        List<SosContact> contacts = contactAssembler.assemble(
                request.situation(),
                trip.getDestinationCountry().getCode(),
                request.location());

        return new SosContactsRes(contacts);
    }

    private Trip loadOwnedTrip(Long userId, Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(TripNotFoundException::new);
        if (!trip.getUser().getId().equals(userId)) {
            throw new TripAccessDeniedException();
        }
        return trip;
    }
}
