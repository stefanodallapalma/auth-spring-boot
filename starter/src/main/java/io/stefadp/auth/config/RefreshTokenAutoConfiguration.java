package io.stefadp.auth.config;

import io.stefadp.auth.core.repository.RefreshTokenRepository;
import io.stefadp.auth.core.repository.RevokedTokenRepository;
import io.stefadp.auth.core.token.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.temporal.ChronoUnit;

@Configuration
public class RefreshTokenAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder defaultPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(RefreshTokenGenerator.class)
    public RefreshTokenGenerator defaultRefreshTokenGenerator() {
        return new SecureRandomRefreshTokenGenerator(new SecureRandom(), 16);
    }

    @Bean
    @ConditionalOnMissingBean(RefreshTokenSettings.class)
    public RefreshTokenSettings defaultRefreshTokenSettings() {
        return new RefreshTokenSettings.Builder()
                .expirationTime(7)
                .expirationTimeUnit(ChronoUnit.DAYS)
                .build();
    }

    @Bean
    public RefreshTokenStore refreshTokenStore(
            PasswordEncoder passwordEncoder,
            RefreshTokenRepository refreshTokenRepository
    ) {
        return new RefreshTokenStore(passwordEncoder, refreshTokenRepository);
    }

    @Bean
    public RefreshTokenManagementService refreshTokenManagementService(
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenSettings refreshTokenSettings,
            RefreshTokenStore refreshTokenStore
    ) {
        return new RefreshTokenManagementService(refreshTokenGenerator, refreshTokenSettings, refreshTokenStore);
    }

    @Bean
    public TokenRevocationService tokenRevocationService(RevokedTokenRepository revokedTokenRepository) {
        return new TokenRevocationService(revokedTokenRepository);
    }
}
