package io.stefadp.auth.core.token;

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