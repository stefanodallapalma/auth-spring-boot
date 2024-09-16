package io.stefadp.auth.core.token;

import jakarta.annotation.Nonnull;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import java.time.temporal.ChronoUnit;

/**
 * Configuration settings for JWT (JSON Web Token) generation and validation.
 * This class provides customizable settings for JWT expiration, issuer, and token claim providers such as roles and subjects.
 */
public record JwtSettings(
        int expirationTime,
        ChronoUnit expirationTimeUnit,
        String issuer,
        MacAlgorithm macAlgorithm,
        String secret
) {

    /**
     * Interface for the first step of the builder, where the secret and the MAC algorithm must be provided.
     */
    public interface SignatureStep {
        /**
         * Sets the secret used for signing the JWT, along with the MAC algorithm.
         * <p>
         * The secret should be a secure, random string of sufficient length.
         * It can be represented as a Base64-encoded string, a hexadecimal string, or a plain alphanumeric string.
         * For `HS512`, a 64-byte (512-bit) secret is recommended.
         * <p>
         * Example formats:
         * <ul>
         *     <li>Alphanumeric String: "mySuperSecretKey1234567890"</li>
         *     <li>Base64-Encoded String: "U3VwZXJTZWNyZXRLZXkxMjM0NTY3ODkwQGZkZw=="</li>
         *     <li>Hexadecimal String: "4d7953757065725365637265744b657932313233343536373839304061626364"</li>
         * </ul>
         * <p>
         * It is critical to ensure that this secret is stored securely and is not hardcoded in production environments.
         *
         * @param secret       The secret to sign the JWT.
         * @param macAlgorithm The MAC algorithm used for signing the JWT.
         * @return The next step in the builder where the expiration time can be set.
         */
        ExpirationTimeStep secretAndAlgorithm(@Nonnull String secret, @Nonnull MacAlgorithm macAlgorithm);
    }

    /**
     * Interface for the second step of the builder where the expiration time can be set.
     */
    public interface ExpirationTimeStep {
        /**
         * Sets the expiration time for the JWT.
         *
         * @param expirationTime The expiration time in the given {@link ChronoUnit}.
         * @return The next step in the builder where the expiration time unit can be set.
         */
        ExpirationTimeUnitStep expirationTime(int expirationTime);

        /**
         * Uses the default expiration time of 1 unit.
         *
         * @return The next step in the builder where the expiration time unit can be set.
         */
        default ExpirationTimeUnitStep defaultExpirationTime() {
            return expirationTime(1);
        }
    }

    /**
     * Interface for the third step of the builder where the expiration time unit can be set.
     */
    public interface ExpirationTimeUnitStep {
        /**
         * Sets the unit of time for the expiration time.
         *
         * @param expirationTimeUnit The {@link ChronoUnit} for the expiration time.
         * @return The next step in the builder where the issuer can be set.
         */
        IssuerStep expirationTimeUnit(ChronoUnit expirationTimeUnit);

        /**
         * Uses the default expiration time unit of {@link ChronoUnit#HOURS}.
         *
         * @return The next step in the builder where the issuer can be set.
         */
        default IssuerStep defaultExpirationTimeUnit() {
            return expirationTimeUnit(ChronoUnit.HOURS);
        }
    }

    /**
     * Interface for the fourth step of the builder where the issuer can be set.
     */
    public interface IssuerStep {
        /**
         * Sets the issuer of the JWT.
         *
         * @param issuer The issuer of the JWT.
         * @return The final step in the builder where the JWT settings can be built.
         */
        BuildStep issuer(String issuer);

        /**
         * Uses the default issuer "self".
         *
         * @return The final step in the builder where the JWT settings can be built.
         */
        default BuildStep defaultIssuer() {
            return issuer("self");
        }
    }

    /**
     * Interface for the final build step.
     */
    public interface BuildStep {
        /**
         * Builds the {@link JwtSettings} instance with the provided configurations.
         *
         * @return A configured {@link JwtSettings} instance.
         */
        JwtSettings build();
    }

    /**
     * Static method to begin the building process.
     * The first step requires the secret and MAC algorithm to be provided.
     *
     * @return The first step of the builder where the secret and MAC algorithm must be set.
     */
    public static SignatureStep builder() {
        return new Builder();
    }

    /**
     * Static nested builder class implementing all steps of the builder pattern.
     */
    public static class Builder implements SignatureStep, ExpirationTimeStep, ExpirationTimeUnitStep, IssuerStep, BuildStep {

        private int expirationTime = 1;
        private ChronoUnit expirationTimeUnit = ChronoUnit.HOURS;
        private String issuer = "self";
        private MacAlgorithm macAlgorithm;
        private String secret;

        @Override
        public ExpirationTimeStep secretAndAlgorithm(@Nonnull String secret, @Nonnull MacAlgorithm macAlgorithm) {
            this.secret = secret;
            this.macAlgorithm = macAlgorithm;
            return this;
        }

        @Override
        public ExpirationTimeUnitStep expirationTime(int expirationTime) {
            if (expirationTime > 0) {
                this.expirationTime = expirationTime;
            }
            return this;
        }

        @Override
        public IssuerStep expirationTimeUnit(ChronoUnit expirationTimeUnit) {
            if (expirationTimeUnit != null) {
                this.expirationTimeUnit = expirationTimeUnit;
            }
            return this;
        }

        @Override
        public BuildStep issuer(String issuer) {
            if (issuer != null) {
                this.issuer = issuer;
            }
            return this;
        }

        @Override
        public JwtSettings build() {
            return new JwtSettings(
                    expirationTime,
                    expirationTimeUnit,
                    issuer,
                    macAlgorithm,
                    secret
            );
        }
    }
}