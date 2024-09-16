package io.stefadp.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.stefadp.auth.core.model.RevokedToken;
import io.stefadp.auth.core.repository.RefreshTokenRepository;
import io.stefadp.auth.core.token.JwtTokenService;
import io.stefadp.auth.core.token.TokenRevocationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;


public class DefaultJwtValidityFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenRevocationService tokenRevocationService;

    public DefaultJwtValidityFilter(
            JwtTokenService jwtTokenService,
            RefreshTokenRepository refreshTokenRepository,
            TokenRevocationService tokenRevocationService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenRevocationService = tokenRevocationService;
    }

    /**
     * Filters incoming requests to validate the JWT token against several security checks.
     * The filter attempts to extract the JWT token from the Authorization header. If no token is found,
     * the filter chain continues, allowing access to public endpoints or those configured with "permitAll()".
     * If a token is present, the filter performs the following checks:
     * <ul>
     * <li>
     *     The access token (i.e., base64-encoded JWT) is checked for expiration. If the token is expired,
     *     the request is rejected with an unauthorized status, and the filter chain is halted.
     * </li>
     * <li>
     *     The access token is checked for revocation. If the token has been revoked,
     *     the request is rejected with an unauthorized status, and the filter chain is halted.
     * </li>
     * <li>
     *     The filter checks if the user associated with the JWT token has no active refresh token assigned.
     *     If no active refresh token is found, the access token is proactively revoked, and the request
     *     is rejected with an unauthorized status, halting the filter chain.
     * </li>
     * </ul>
     * If any of these checks fail, the filter responds with an unauthorized status and prevents further processing of the request.
     * If all checks pass, the request is allowed to proceed with a valid token.
     *
     * @param request     The servlet request.
     * @param response    The servlet response.
     * @param filterChain The filter chain.
     * @throws ServletException in case of a servlet-related error during the request processing.
     * @throws IOException      in case of an I/O error during the request processing.
     */
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = extractTokenFromRequest(request);

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String subject = jwtTokenService.getSubject(accessToken);
        Instant exp = jwtTokenService.getExpiresAt(accessToken);

        boolean isTokenExpired = exp.isBefore(Instant.now());
        if (isTokenExpired) {
            respondWithUnauthorized(response, "This JWT token has expired");
            return;
        }

        boolean isTokenRevoked = tokenRevocationService.isTokenRevoked(accessToken);
        if (isTokenRevoked) {
            respondWithUnauthorized(response, "This JWT token has been revoked");
            return;
        }

        boolean noActiveRefreshTokenForUser = !refreshTokenRepository.existsBySubject(subject);
        if (noActiveRefreshTokenForUser) {
            tokenRevocationService.revokeToken(new RevokedToken(accessToken, exp));
            respondWithUnauthorized(response, "Revoking access token because refresh token was revoked.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header of the request.
     * If the header is either not present or does not contain a Bearer token, the method returns null.
     *
     * @param request The servlet request.
     * @return The extracted JWT token, or null if the bearer token is not present or is incorrectly formatted.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7); // "Bearer " is 7 characters
    }

    /**
     * Responds to the client with an unauthorized status and a custom message.
     *
     * @param response The servlet response to which the unauthorized status and message are written.
     * @param message  The message to include in the response.
     * @throws IOException if an I/O error occurs while writing the response.
     */
    private void respondWithUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        try (PrintWriter writer = response.getWriter()) {
            writer.write(message);
        }
    }
}
