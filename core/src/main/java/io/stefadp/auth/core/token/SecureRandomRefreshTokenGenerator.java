package io.stefadp.auth.core.token;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * A class responsible for generating secure random refresh tokens.
 */
public class SecureRandomRefreshTokenGenerator implements RefreshTokenGenerator {

    private final SecureRandom secureRandom;
    private final int byteLength;
    private final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    /**
     * Constructs a new instance of {@code SecureRandomRefreshTokenGenerator} with the specified
     * {@code SecureRandom} instance and byte length.
     *
     * @param secureRandom The secure random instance used to generate random bytes.
     * @param byteLength   The length of the byte array to generate the token.
     */
    public SecureRandomRefreshTokenGenerator(SecureRandom secureRandom, int byteLength) {
        this.secureRandom = secureRandom;
        this.byteLength = byteLength;
    }

    /**
     * Generates a secure random refresh token encoded in URL-safe Base64.
     *
     * @return The generated refresh token as a URL-safe Base64 encoded string.
     */
    @Override
    public String generate() {
        byte[] randomBytes = new byte[byteLength];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}