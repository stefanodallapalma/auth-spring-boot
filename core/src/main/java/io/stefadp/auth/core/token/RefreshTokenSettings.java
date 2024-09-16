package io.stefadp.auth.core.token;

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