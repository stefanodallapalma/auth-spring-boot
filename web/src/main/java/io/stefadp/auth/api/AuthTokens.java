package io.stefadp.auth.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthTokens(
        @NotEmpty
        String accessToken,
        @NotEmpty
        String refreshToken
) {
}
