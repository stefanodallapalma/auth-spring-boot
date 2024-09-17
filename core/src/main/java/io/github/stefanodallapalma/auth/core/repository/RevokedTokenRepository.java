package io.github.stefanodallapalma.auth.core.repository;

import io.github.stefanodallapalma.auth.core.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByTokenValue(String tokenValue);
}
