package io.stefadp.auth.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.stefadp.auth.core.token.JwtSettings;
import io.stefadp.auth.core.token.JwtTokenService;
import io.stefadp.auth.core.token.RefreshTokenStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtAutoConfiguration {

    private static final String DEFAULT_JWT_SECRET = "f33328a0b388419003188ed97b6ebfb25dd5983ccca4936b6ff18c9f6f6023fb";

    @Bean
    @ConditionalOnMissingBean(JwtSettings.class)
    public JwtSettings defaultJwtSettings() {
        return new JwtSettings.Builder()
                .secretAndAlgorithm(DEFAULT_JWT_SECRET, MacAlgorithm.HS512)
                .defaultExpirationTime()
                .defaultExpirationTimeUnit()
                .defaultIssuer()
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(JwtEncoder.class)
    public JwtEncoder jwtEncoder(JwtSettings jwtSettings) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSettings.secret().getBytes()));
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder(JwtSettings jwtSettings) {
        var bytes = jwtSettings.secret().getBytes();
        SecretKey secretKey = new SecretKeySpec(bytes, 0, bytes.length, "RSA");
        return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(jwtSettings.macAlgorithm()).build();
    }

    @Bean
    public JwtTokenService jwtTokenService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            JwtSettings jwtSettings,
            RefreshTokenStore refreshTokenStore) {
        return new JwtTokenService(jwtEncoder, jwtDecoder, jwtSettings, refreshTokenStore);
    }
}
