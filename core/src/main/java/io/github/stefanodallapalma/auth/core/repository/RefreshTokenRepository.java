package io.github.stefanodallapalma.auth.core.repository;

import io.github.stefanodallapalma.auth.core.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    boolean existsBySubject(String subject);

    Optional<RefreshToken> findBySubject(String subject);

    Optional<RefreshToken> findByTokenValue(String tokenValue);
}
