package io.stefadp.auth.core.token;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import io.stefadp.auth.core.model.RefreshToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service class responsible for managing refresh tokens, including creation, validation,
 * refreshing, and invalidation of tokens. This service interacts with the token store
 * and generator to ensure secure handling of refresh tokens.
 */
@Service
public class RefreshTokenManagementService {
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenSettings refreshTokenSettings;
    private final RefreshTokenStore refreshTokenStore;

    /**
     * Constructs a new {@code RefreshTokenManagementService} with the provided {@code RefreshTokenGenerator}
     * and {@code RefreshTokenStore}.
     *
     * @param refreshTokenGenerator The generator used to create new refresh tokens.
     * @param refreshTokenSettings  The settings for refresh token validity.
     * @param refreshTokenStore     The service used for storing and managing refresh tokens.
     */
    @Autowired
    public RefreshTokenManagementService(
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenSettings refreshTokenSettings,
            RefreshTokenStore refreshTokenStore) {
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenSettings = refreshTokenSettings;
        this.refreshTokenStore = refreshTokenStore;
    }

    /**
     * Creates the refresh token for the given subject. If a refresh token already exists for the subject,
     * it is deleted before creating and storing a new one.
     *
     * @param subject The subject (usually the username) for which the refresh token will be refreshed.
     * @return A copy of the new refresh token (no ID included).
     */
    @Transactional
    public RefreshToken create(@Nonnull String subject) {
        // Create and store a new refresh token
        RefreshToken newRefreshToken = new RefreshToken(
                refreshTokenGenerator.generate(),
                Instant.now().plus(refreshTokenSettings.expirationTime(), refreshTokenSettings.expirationTimeUnit()),
                subject
        );

        // Delete any existing token for this subject
        refreshTokenStore.deleteTokenBySubject(subject);
        refreshTokenStore.store(newRefreshToken);
        return newRefreshToken; // Return the new token for client-side usage
    }

    /**
     * Refreshes the refresh token for the given token value. If a refresh token is found and is still valid,
     * it is deleted before creating and storing a new one.
     *
     * @param oldTokenValue The token value of the refresh token that is to be refreshed.
     * @return A copy of the new refresh token (no ID included), or {@code null} if the old token is invalid or expired.
     */
    @Nullable
    public RefreshToken refresh(@Nonnull String oldTokenValue) {
        // Refreshing the token should be authorized even when the user is not authenticated,
        // as its purpose is to re-authenticate the user when the access token has expired.
        // However, a valid refresh token is required to obtain a new one.
        // If the refresh token is not valid, the user is required to authenticate using standard credentials.
        var token = refreshTokenStore.retrieveToken(oldTokenValue);
        if(token.isEmpty()) return null;
        if(token.get().hasExpired()) return null;

        return create(token.get().getSubject());


//        return refreshTokenStore.retrieveToken(oldTokenValue)
//                .filter(RefreshToken::hasNotExpired)
//                .map(refreshToken -> create(refreshToken.getSubject())) // Proceed to delete old token and create new one
//                .orElse(null);
    }

    /**
     * Invalidates (deletes) a refresh token by its token value.
     *
     * @param tokenValue The value of the token that needs to be invalidated.
     */
    public void invalidateByTokenValue(@Nonnull String tokenValue) {
        refreshTokenStore.deleteTokenByTokenValue(tokenValue);
    }

    /**
     * Invalidates (deletes) all refresh tokens associated with a specific subject.
     *
     * @param subject The subject (usually the username) whose tokens need to be invalidated.
     */
    public void invalidateBySubject(@Nonnull String subject) {
        refreshTokenStore.deleteTokenBySubject(subject);
    }
}