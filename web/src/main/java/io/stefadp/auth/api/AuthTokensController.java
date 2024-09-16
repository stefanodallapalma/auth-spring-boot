package io.stefadp.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthTokensController {
    private final AuthTokensManagementFacade authTokensManagementFacade;

    public AuthTokensController(AuthTokensManagementFacade authTokensFacade) {
        this.authTokensManagementFacade = authTokensFacade;
    }

    @Operation(
            summary = "Generate a new pair of access and refresh tokens",
            description = "This endpoint is used to refresh the access and refresh tokens with token rotation. " +
                    "The client sends the current refresh token, and if it's valid, " +
                    "the server will respond with a new pair of access and refresh tokens.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Request body containing the current refresh token",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "New access and refresh tokens generated successfully",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthTokens.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
                                                              "refreshToken": "riWm6YA5UM853VXQp7NlrQ=="
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            description = "Bad Request (e.g., malformed request body)",
                            responseCode = "400"
                    ),
                    @ApiResponse(
                            description = "Unauthorized (e.g., invalid or expired refresh token)",
                            responseCode = "401"
                    )
            }
    )
    @PutMapping("/auth_tokens")
    ResponseEntity<AuthTokens> refreshAuthTokens(
            @RequestBody
            @Valid
            RefreshTokenRequest refreshTokensRequest
    ) {
        return ResponseEntity.ok(authTokensManagementFacade.refreshAuthTokens(refreshTokensRequest.refreshToken()));
    }

    @Operation(
            summary = "Delete Authentication Tokens",
            description = "This endpoint invalidates both the access and refresh tokens associated with the authenticated user. "
                    + "The client must provide a valid JWT authentication token in the request, along with the refresh token. "
                    + "Upon successful deletion, the server will respond with a 204 No Content status.",
            responses = {
                    @ApiResponse(
                            description = "Tokens successfully invalidated",
                            responseCode = "204"
                    ),
                    @ApiResponse(
                            description = "Bad Request (e.g., invalid token)",
                            responseCode = "400"
                    ),
                    @ApiResponse(
                            description = "Unauthorized (e.g., invalid or expired JWT)",
                            responseCode = "401"
                    )
            }
    )
    @DeleteMapping("/auth_tokens")
    public ResponseEntity<Void> deleteAuthTokens(
            AbstractOAuth2TokenAuthenticationToken<Jwt> jwtToken
    ) {
        if (jwtToken == null) throw new IllegalArgumentException("JWT token should not be null");
        authTokensManagementFacade.deleteAuthTokens(jwtToken);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Refresh access token",
            description = "This endpoint is used to refresh the access token. " +
                    "The client must provide a valid JWT authentication token " +
                    "and the corresponding refresh token. If the refresh token is valid and " +
                    "matches the JWT subject, a new access token will be generated and returned.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Request body containing the current refresh token",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "New access token generated successfully",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthTokens.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "accessToken": "eyJhbGciOiJIUzUxMiJ9..."
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            description = "Bad Request (e.g., malformed request body)",
                            responseCode = "400"
                    ),
                    @ApiResponse(
                            description = "Unauthorized (e.g., invalid or expired refresh token, or mismatch between JWT subject and refresh token owner)",
                            responseCode = "401"
                    )
            }
    )
    @PutMapping("/access_token")
    ResponseEntity<AuthTokens> refreshAccessTokenOnly(
            @RequestBody
            @Valid
            RefreshTokenRequest refreshTokensRequest
    ) {
        return ResponseEntity.ok(authTokensManagementFacade.refreshAccessToken(refreshTokensRequest.refreshToken()));
    }
}
