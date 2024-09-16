package io.stefadp.auth.core.repository;

import io.stefadp.auth.core.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    boolean existsBySubject(String subject);

    Optional<RefreshToken> findBySubject(String subject);
    Optional<RefreshToken> findByTokenValue(String tokenValue);
}
