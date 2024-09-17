package io.github.stefanodallapalma.auth.config;

import io.github.stefanodallapalma.auth.core.token.JwtTokenService;
import io.github.stefanodallapalma.auth.core.token.RefreshTokenManagementService;
import io.github.stefanodallapalma.auth.core.token.TokenRevocationService;
import io.github.stefanodallapalma.auth.api.AuthTokensController;
import io.github.stefanodallapalma.auth.api.AuthTokensManagementFacade;
import io.github.stefanodallapalma.auth.core.repository.RefreshTokenRepository;
import io.github.stefanodallapalma.auth.filter.DefaultJwtValidityFilter;
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
