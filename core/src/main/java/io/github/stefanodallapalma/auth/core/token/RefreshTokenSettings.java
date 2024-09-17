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

import java.time.temporal.ChronoUnit;

/**
 * This class provides customizable settings for refresh token expiration.
 */
public record RefreshTokenSettings(int expirationTime, ChronoUnit expirationTimeUnit) {


    /**
     * Builder class for creating instances of {@link RefreshTokenSettings}.
     */
    public static class Builder {
        private int expirationTime = 7;
        private ChronoUnit expirationTimeUnit = ChronoUnit.DAYS;


        public Builder expirationTime(int expirationTime) {
            if (expirationTime > 0) {
                this.expirationTime = expirationTime;
            }
            return this;
        }

        public Builder expirationTimeUnit(ChronoUnit expirationTimeUnit) {
            if (expirationTimeUnit != null) {
                this.expirationTimeUnit = expirationTimeUnit;
            }
            return this;
        }

        /**
         * Builds and returns a new {@link RefreshTokenSettings} instance.
         *
         * @return A configured {@link RefreshTokenSettings} instance.
         */
        public RefreshTokenSettings build() {
            return new RefreshTokenSettings(expirationTime, expirationTimeUnit);
        }
    }
}