package io.stefadp.auth.core.token;

import io.stefadp.auth.core.model.RevokedToken;
import io.stefadp.auth.core.repository.RevokedTokenRepository;

/**
 * Service responsible for managing the revocation of access and refresh tokens.
 * It provides methods to revoke a token and check whether a token has been revoked.
 */
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Constructs a new instance of {@code TokenRevocationService} with the specified
     * {@code RevokedTokenRepository}.
     *
     * @param revokedTokenRepository The repository used for accessing and modifying revoked token data.
     */
    public TokenRevocationService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    /**
     * Marks the provided token as revoked and updates it in the repository.
     *
     * @param revokedToken The token entity to be revoked.
     */
    public void revokeToken(RevokedToken revokedToken) {
        if (!isTokenRevoked(revokedToken.getTokenValue())) {
            revokedTokenRepository.save(revokedToken);
        }
    }

    /**
     * Checks whether a given token has been revoked.
     *
     * @param tokenValue The value of the token to be checked for revocation.
     * @return {@code true} if the token has been revoked, {@code false} otherwise.
     */
    public boolean isTokenRevoked(String tokenValue) {
        return revokedTokenRepository.existsByTokenValue(tokenValue);
    }
}