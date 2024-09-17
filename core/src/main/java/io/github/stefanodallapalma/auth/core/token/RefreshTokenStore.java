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

import io.github.stefanodallapalma.auth.core.model.RefreshToken;
import io.github.stefanodallapalma.auth.core.repository.RefreshTokenRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


/**
 * Service class responsible for managing low-level operations to securely store, retrieve, and delete refresh tokens.
 */
public class RefreshTokenStore {
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Constructs a new {@code RefreshTokenStore} with the provided {@code PasswordEncoder} and {@code RefreshTokenRepository}.
     *
     * @param passwordEncoder        The password encoder used to encrypt token values.
     * @param refreshTokenRepository The repository for storing and retrieving refresh tokens.
     */
    public RefreshTokenStore(PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository) {
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Deletes the refresh token associated with the specified subject.
     *
     * @param subject The subject whose token should be deleted.
     */
    public void deleteTokenBySubject(String subject) {
        refreshTokenRepository.findBySubject(subject).ifPresent(it -> {
            refreshTokenRepository.delete(it);
            refreshTokenRepository.flush();
        });
    }

    /**
     * Deletes the refresh token associated with the specified token value.
     *
     * @param tokenValue The plain value of the token to be removed.
     */
    public void deleteTokenByTokenValue(String tokenValue) {
        retrieveToken(tokenValue).ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Retrieves a refresh token from the repository by its token value.
     * This method decrypts the stored token values and matches them with the provided plain token value.
     *
     * @param tokenValue The plain value of the token to retrieve.
     * @return An {@code Optional} containing the matching {@code RefreshToken} if found, or empty if not found.
     */
    public Optional<RefreshToken> retrieveToken(String tokenValue) {
        Pageable pageable = PageRequest.of(0, 1000); // Start with the first page and 1000 records per page
        Page<RefreshToken> page;

        do {
            page = refreshTokenRepository.findAll(pageable);

            // Attempt to find a match in the current page
            Optional<RefreshToken> matchedToken = page.stream()
                    .filter(token -> this.passwordEncoder.matches(tokenValue, token.getTokenValue()))
                    .findFirst();

            if (matchedToken.isPresent()) {
                return matchedToken;
            }

            pageable = pageable.next();

        } while (page.hasNext());

        return Optional.empty();
    }

    /**
     * Encrypts the value of the provided refresh token and stores it in the repository.
     * If a token already exists for the same subject, it is deleted before storing the new token.
     *
     * @param refreshToken The {@code RefreshToken} to encrypt and store.
     */
    public void store(RefreshToken refreshToken) {
        RefreshToken encryptedRefreshToken = new RefreshToken(
                passwordEncoder.encode(refreshToken.getTokenValue()),
                refreshToken.getExpiresAt(),
                refreshToken.getSubject()
        );

        refreshTokenRepository.save(encryptedRefreshToken);
    }
}