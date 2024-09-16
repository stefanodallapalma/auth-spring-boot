package io.stefadp.auth.core.token;

import jakarta.annotation.Nonnull;
import io.stefadp.auth.core.model.RefreshToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtSettings jwtSettings;
    private final RefreshTokenStore refreshTokenStore;

    @Autowired
    public JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            JwtSettings jwtSettings,
            RefreshTokenStore refreshTokenStore
    ){
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtSettings = jwtSettings;
        this.refreshTokenStore = refreshTokenStore;
    }

    /**
     * Generates a JWT token for the given user. The token contains the user's roles
     * and is set to expire after 1 hour.
     *
     * @param subject The user subject for which the JWT token should be generated.
     * @return The generated JWT token as a string.
     */
    public String create(String subject) {
        var now = Instant.now();

        var claims = JwtClaimsSet.builder()
                .expiresAt(now.plus(jwtSettings.expirationTime(), jwtSettings.expirationTimeUnit()))
                .issuer(jwtSettings.issuer())
                .issuedAt(now)
                .subject(subject)
                .build();

        var encoderParameters = JwtEncoderParameters.from(JwsHeader.with(jwtSettings.macAlgorithm()).build(), claims);
        return jwtEncoder.encode(encoderParameters).getTokenValue();
    }

    /**
     * Refreshes the JWT (access token) for the authenticated user, provided a valid refresh token is supplied.
     * The refresh token must exist and the subject associated with the refresh token must match the subject
     * in the provided JWT. If these conditions are met, a new JWT is generated and returned.
     * If not, an exception is thrown.
     *
     * @param refreshTokenValue The value of the refresh token provided by the user.
     * @return A new JWT (access token) for the user if the refresh token is valid and matches the JWT's subject.
     * @throws IllegalArgumentException if the refresh token does not exist or does not match the subject in the JWT.
     */
    public String refresh(@Nonnull String refreshTokenValue) {
        return refreshTokenStore.retrieveToken(refreshTokenValue)
                .filter(RefreshToken::hasNotExpired)
                .map(refreshToken -> this.create(refreshToken.getSubject()))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token was not found or expired"));
    }

    public String getSubject(String encodedJwt) {
        return jwtDecoder.decode(encodedJwt).getSubject();
    }

    public Instant getExpiresAt(String encodedJwt) {
        var defaultExpiresAt = Instant.now().plus(jwtSettings.expirationTime(), jwtSettings.expirationTimeUnit());
        var actualExpiresAt = jwtDecoder.decode(encodedJwt).getExpiresAt();
        return actualExpiresAt == null
                ? defaultExpiresAt
                : actualExpiresAt;
    }
}
