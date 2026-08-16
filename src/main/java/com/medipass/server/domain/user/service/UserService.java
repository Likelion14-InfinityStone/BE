package com.medipass.server.domain.user.service;

import com.medipass.server.domain.user.entity.User;
import com.medipass.server.domain.user.repository.UserRepository;
import com.medipass.server.domain.user.web.dto.UserAccountRes;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserAccountRes getAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(
                        ErrorResponseCode.NOT_FOUND_RESOURCE,
                        "사용자 정보를 찾을 수 없습니다."
                ));

        return UserAccountRes.from(user);
    }
}
