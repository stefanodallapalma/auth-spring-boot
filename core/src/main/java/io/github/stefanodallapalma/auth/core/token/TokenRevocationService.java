package io.github.stefanodallapalma.auth.core.token;

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

import io.github.stefanodallapalma.auth.core.model.RevokedToken;
import io.github.stefanodallapalma.auth.core.repository.RevokedTokenRepository;

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