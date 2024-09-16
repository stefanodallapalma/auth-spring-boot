package io.stefadp.auth.config;

import io.stefadp.auth.api.AuthTokensController;
import io.stefadp.auth.api.AuthTokensManagementFacade;
import io.stefadp.auth.core.repository.RefreshTokenRepository;
import io.stefadp.auth.core.token.*;
import io.stefadp.auth.filter.DefaultJwtValidityFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAutoConfiguration {

    @Bean
    public AuthTokensManagementFacade authTokensManagementFacade(
            JwtTokenService jwtTokenService,
            RefreshTokenManagementService refreshTokenManagementService,
            TokenRevocationService tokenRevocationService
    ) {
        return new AuthTokensManagementFacade(jwtTokenService, refreshTokenManagementService, tokenRevocationService);
    }

    @Bean
    public AuthTokensController authTokensController(AuthTokensManagementFacade authTokensManagementFacade) {
        return new AuthTokensController(authTokensManagementFacade);
    }

    @Bean
    public DefaultJwtValidityFilter defaultJwtValidityFilter(
            JwtTokenService jwtTokenService,
            RefreshTokenRepository refreshTokenRepository,
            TokenRevocationService tokenRevocationService) {
        return new DefaultJwtValidityFilter(jwtTokenService, refreshTokenRepository, tokenRevocationService);
    }
}
