package io.stefadp.auth.core.token;

import io.stefadp.auth.core.model.RefreshToken;
import io.stefadp.auth.core.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;


/**
 * Service class responsible for managing low-level operations to securely store, retrieve, and delete refresh tokens.
 */
@Component
public class RefreshTokenStore {
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Constructs a new {@code RefreshTokenStore} with the provided {@code PasswordEncoder} and {@code RefreshTokenRepository}.
     *
     * @param passwordEncoder        The password encoder used to encrypt token values.
     * @param refreshTokenRepository The repository for storing and retrieving refresh tokens.
     */
    @Autowired
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