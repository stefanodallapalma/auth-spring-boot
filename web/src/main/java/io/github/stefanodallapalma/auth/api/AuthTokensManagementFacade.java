package io.github.stefanodallapalma.auth.api;

/* Copyright 2024 Stefano Dalla Palma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import io.github.stefanodallapalma.auth.core.model.RefreshToken;
import io.github.stefanodallapalma.auth.core.model.RevokedToken;
import io.github.stefanodallapalma.auth.core.token.JwtTokenService;
import io.github.stefanodallapalma.auth.core.token.RefreshTokenManagementService;
import io.github.stefanodallapalma.auth.core.token.TokenRevocationService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import java.time.Instant;

/**
 * The {@code AuthTokensFacade} class is the primary entry point for managing authentication tokens within the application.
 * This service provides methods for creating, refreshing, and deleting both access and refresh tokens.
 * It interacts with underlying services responsible for handling JWT creation, refresh token management,
 * and token revocation, encapsulating the logic required to manage authentication tokens securely.
 * <p>
 * The typical use cases include:
 * <ul>
 *     <li>Creating a new pair of access and refresh tokens for a given subject (usually a username).</li>
 *     <li>Refreshing the access token using a valid refresh token.</li>
 *     <li>Refreshing both access and refresh tokens if the refresh token is valid and not expired.</li>
 *     <li>Revoking both access and refresh tokens for a given subject, typically during logout.</li>
 * </ul>
 * <p>
 * This service is intended to be used by consumers of the library to handle token-based authentication seamlessly.
 */
public class AuthTokensManagementFacade {
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenManagementService refreshTokenManagementService;
    private final TokenRevocationService tokenRevocationService;

    public AuthTokensManagementFacade(
            JwtTokenService jwtTokenService,
            RefreshTokenManagementService refreshTokenManagementService,
            TokenRevocationService tokenRevocationService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenManagementService = refreshTokenManagementService;
        this.tokenRevocationService = tokenRevocationService;
    }

    /**
     * Creates a new pair of access and refresh tokens for the given subject.
     * This method generates a new JWT access token and a corresponding refresh token for the specified subject.
     *
     * @param subject the subject (typically the username) for whom the tokens are being created.
     * @return an {@link AuthTokens} object containing the newly created access and refresh tokens.
     */
    @Transactional
    public AuthTokens createAuthTokens(@Nonnull String subject) {
        RefreshToken refreshToken = refreshTokenManagementService.create(subject);
        String accessToken = jwtTokenService.create(subject);
        return new AuthTokens(accessToken, refreshToken.getTokenValue());
    }

    /**
     * Deletes both access and refresh tokens associated with the authenticated user.
     * This method revokes the current access token and invalidates the associated refresh token,
     * effectively logging the user out.
     *
     * @param jwt the {@link AbstractOAuth2TokenAuthenticationToken} containing the current JWT and its associated subject.
     */
    @Transactional
    public void deleteAuthTokens(@Nonnull AbstractOAuth2TokenAuthenticationToken<Jwt> jwt) {
        String accessTokenValue = jwt.getToken().getTokenValue();
        String subject = jwtTokenService.getSubject(accessTokenValue);
        Instant expiresAt = jwtTokenService.getExpiresAt(accessTokenValue);

        tokenRevocationService.revokeToken(new RevokedToken(accessTokenValue, expiresAt));
        refreshTokenManagementService.invalidateBySubject(subject);
    }

    /**
     * Refreshes both the access and refresh tokens using the provided refresh token value.
     * This method is typically used when implementing refresh token rotation, where a new
     * refresh token is issued each time a token is refreshed, and the old refresh token is invalidated.
     *
     * The method validates the provided refresh token. If the refresh token is valid and not expired,
     * this method generates and returns a new pair of access and refresh tokens. If the refresh token is invalid
     * or expired, an {@link IllegalArgumentException} is thrown.
     *
     * @param refreshTokenValue the value of the refresh token to be validated and used for generating new tokens.
     * @return an {@link AuthTokens} object containing the newly generated access and refresh tokens.
     * @throws IllegalArgumentException if the provided refresh token is invalid, expired, or the user is already signed out.
     */
    @Transactional
    public AuthTokens refreshAuthTokens(@Nonnull String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenManagementService.refresh(refreshTokenValue);
        if (refreshToken == null) {
            throw new IllegalArgumentException("The provided refresh token is invalid. This may be due to the token being expired or the user being already singed out.");
        }

        String accessToken = jwtTokenService.create(refreshToken.getSubject());
        return new AuthTokens(accessToken, refreshToken.getTokenValue());
    }

    /**
     * Refreshes the access token only, using the provided refresh token.
     * This method does not rotate the refresh token; instead, it keeps the existing refresh token unchanged
     * while generating a new access token. This is useful in scenarios where refresh token rotation is not required.
     *
     * The method validates the provided refresh token and ensures that it is still valid. If the refresh token
     * is valid, a new access token is generated and returned with a {@code null} refresh token in the response.
     *
     * @param refreshTokenValue the value of the refresh token to be validated and used for generating a new access token.
     * @return an {@link AuthTokens} object containing the newly generated access token and a {@code null} refresh token.
     * @throws IllegalArgumentException if the refresh token is invalid or does not match the expected subject.
     */
    public AuthTokens refreshAccessToken(@Nonnull String refreshTokenValue) {
        String accessToken = jwtTokenService.refresh(refreshTokenValue);
        return new AuthTokens(accessToken, null);
    }
}
