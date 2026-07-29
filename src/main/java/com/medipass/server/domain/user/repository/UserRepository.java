package com.medipass.server.domain.user.repository;

import com.medipass.server.domain.user.entity.User;
import com.medipass.server.domain.user.entity.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(UserProvider provider, String providerId);
}
