# Authentication and Token Management Library

This library provides an out-of-the-box solution for handling JWT-based authentication, focusing on access and refresh token creation, management, and revocation. It simplifies secure user authentication workflows, including token rotation and token-based logout, while minimizing the need for customization.


## Features

- **JWT Token Generation**: Securely generate JSON Web Tokens (JWT) for authenticated users.
- **Refresh Token Management**: Easily generate, store, and rotate refresh tokens.
- **Token Revocation**: Revoke tokens to prevent further use.
- **Integrated Token Management**: A unified, simple interface for token operations.
- **Built-in Security Filters**: Validate and manage tokens securely, according to best practices.
- **Easy Integration**: Pre-built controllers and filters for easy integration with your Spring Boot application.


## Table of Contents
- [Requirements](#requirements)
- [Getting Started](#getting-started)
    1. [Add Library](#1-add-library)
    2. [Configure JWT Settings](#2-configure-jwt-settings)
    3. [(Optional) Configure Refresh Token Settings](#3-optional-configure-refresh-token-settings)
    4. [Integrate with Your Application](#4-integrate-with-your-application)
- [Key Components](#key-components)
    - [AuthTokensManagementFacade](#authtokensmanagementfacade)
    - [JwtTokenService](#jwttokenservice)
    - [RefreshTokenManagementService](#refreshtokenmanagementservice)
    - [TokenRevocationService](#tokenrevocationservice)
    - [DefaultJwtValidityFilter](#defaultjwtvalidityfilter)
- [Usage Notes](#usage-notes)

## Requirements
To use this library, ensure your project meets the following:
- **Java 17 or higher**.
- **Spring Boot 3.x**.
- **Dependencies**:
    - `spring-boot-starter-security`
    - `spring-boot-starter-web`
    - `spring-boot-starter-data-jpa`
    - `spring-boot-starter-validation`
    - `springdoc-openapi-starter-webmvc-ui`


## Getting Started

### 1. Add Library
Ensure the library is available in your project by adding the JAR file and updating your dependencies.
Download the library and add it to your project’s `libs` directory (a version on Maven Central is coming soon). Then, include it in your `build.gradle` file:
```gradle
dependencies {
    implementation files('libs/auth-spring-boot-starter-0.0.1.jar')
}
```

### 2. Configure JWT Settings
Create a configuration class to manage JWT token generation. This defines important token parameters like the secret, algorithm, and expiration time.

```java
@Configuration
public class JwtConfiguration {

    private static final String JWT_SECRET = "<YOUR JWT SECRET>";

    @Bean
    public JwtSettings jwtSettings() {
        return new JwtSettings.Builder()
                .secretAndAlgorithm(JWT_SECRET, MacAlgorithm.HS512)
                .defaultExpirationTime() // 1
                .defaultExpirationTimeUnit() // ChronoUnit.HOURS
                .defaultIssuer() // "self"
                .build();
    }
}
```
> **Note:** The `JWT_SECRET` should be securely managed. Do not hardcode sensitive values in production environments. Use environment variables or secure vaults.

<details> <summary>Customizing JWT Settings</summary>
If you wish to modify the default settings, you can override values like expiration time and issuer. Below is an example configuration with custom values for token expiration and issuer.

```java
@Bean
public JwtSettings jwtSettings() {
    return new JwtSettings.Builder()
            .secretAndAlgorithm(JWT_SECRET, MacAlgorithm.HS512)
            .expirationTime(30) // Custom expiration time of 30 minutes
            .expirationTimeUnit(ChronoUnit.MINUTES) // Set time unit to minutes
            .defaultIssuer("myorg") // Custom issuer value
            .build();
}
```

#### Customization Options

- `expirationTime(int)`: Set the custom duration for token validity. In this example, tokens expire after 30 minutes.
- `expirationTimeUnit(ChronoUnit)`: Specify the time unit for expiration (e.g., minutes, hours, days). The default is hours, but you can change it to minutes or any other appropriate time unit.
- `defaultIssuer(String)`: Customize the issuer field in the JWT. You can specify any string that represents the entity issuing the token, such as "myorg" in this case

</details>

### 3. (Optional) Configure Refresh Token Settings
<details> <summary>If needed, customize the settings for refresh token management. The defaults should work for most use cases, but here’s how you can modify them.</summary>

```java
@Configuration
public class RefreshTokenConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RefreshTokenGenerator refreshTokenGenerator() {
        return new SecureRandomRefreshTokenGenerator(new SecureRandom(), 16);
    }

    @Bean
    public RefreshTokenSettings refreshTokenSettings() {
        return new RefreshTokenSettings.Builder()
                .expirationTime(7)
                .expirationTimeUnit(ChronoUnit.DAYS)
                .build();
    }
}
```
> **Note:** The `PasswordEncoder` is used to encrypt refresh tokens before storing them securely in the database.
</details>

### 4. Integrate with Your Application
The library provides an out-of-the-box controller (`AuthTokensController`) to manage token refresh and revocation. To use it, you only need to configure your security settings to allow public access to the token endpoints.

- **Create a Login Endpoint**: You'll need a custom endpoint for user authentication (login) and to generate access and refresh tokens. Use the `AuthTokensManagementFacade::createAuthTokens(String)` to simplify this process.

```java
@RestController
@RequestMapping("/auth")
public class MyAuthController {

    private final AuthTokensManagementFacade authTokensFacade;

    @Autowired
    public MyAuthController(AuthTokensManagementFacade authTokensFacade) {
        this.authTokensFacade = authTokensFacade;
    }

    @PostMapping("/login") // Can be any path, also auth_tokens for consistency with the provided controller
    public ResponseEntity<AuthTokens> login(@RequestBody LoginRequest loginRequest) {
        // Authentication logic here
        AuthTokens authTokens = authTokensFacade.createAuthTokens(loginRequest.getUsername());
        return ResponseEntity.ok(authTokens);
    }
}
```
- **Security Configuration**: Ensure that your security configuration allows public access to token refresh endpoints (`PUT /auth/auth_tokens`, `PUT /auth/access_token`), while protecting others. The DELETE endpoint requires bearer authentication, so no further configuration is needed.

```java
// In SecurityConfig.java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
        it ->
            it.requestMatchers(new AntPathRequestMatcher("/auth/login")).permitAll()
              .requestMatchers(new AntPathRequestMatcher("/auth/auth_tokens", "PUT")).permitAll()
              .requestMatchers(new AntPathRequestMatcher("/auth/access_tokens", "PUT")).permitAll() // You may want to skip this if you're already using `PUT /auth/auth_tokens` and vice versa.
              .anyRequest()
              .authenticated());

    // Additional http configs
    return http.build();
}
```


![Alt Text](./docs/preview.gif)

## Key Components

### AuthTokensManagementFacade
The `AuthTokensManagementFacade` provides a high-level interface for creating, refreshing, and revoking tokens.

- **Create Auth Tokens**: Generates new access and refresh tokens.
- **Refresh Auth Tokens**: Rotates access and refresh tokens using a valid refresh token.
- **Delete Auth Tokens**: Revokes both tokens (logging out the user).

### JwtTokenService
The `JwtTokenService` manages JWT creation and validation.
- **Create**: Generates JWTs.
- **Validate**: Checks the validity of the token.
- **Get Subject**: Extracts the user information from the JWT.

### RefreshTokenManagementService
The `RefreshTokenManagementService` manages refresh tokens, including generating, rotating, and invalidating them.

### TokenRevocationService
The `TokenRevocationService` manages token revocation, ensuring that revoked tokens are no longer accepted in requests.

### DefaultJwtValidityFilter

The `DefaultJwtValidityFilter` plays a crucial role in securing your application by orchestrating the validation of JWT and refresh tokens for all incoming requests. It ensures that tokens used for authentication are valid, unexpired, and have not been revoked. This filter is automatically applied to all routes that require authentication, providing a secure and efficient token validation mechanism.

Key features of the filter include:

- **Token Expiration Check**: The filter ensures that the JWT token has not expired. If the token is expired, the request is immediately rejected with an unauthorized status, preventing further processing.
  
- **Token Revocation Check**: If a token has been explicitly revoked, either by an administrator or due to user logout, the filter will recognize this and reject the request. Revoked tokens are stored in a dedicated database table (`revoked_tokens`), ensuring that even if a token is valid by expiration standards, it will still be rejected if it was revoked.

- **Refresh Token Validation**: The filter also verifies that the user associated with the JWT token has an active refresh token. If no active refresh token exists for that user, the filter proactively revokes the JWT and prevents further access. This ensures that tokens cannot be used after a user’s refresh token has been invalidated.

#### Automatic Database Table Creation

To support these features, the library automatically creates two database tables during initialization:

1. **`refresh_tokens` Table**: This table keeps track of all generated refresh tokens. Every time a refresh token is created, it is stored here, allowing the system to validate if a user’s refresh token is still active during authentication.

2. **`revoked_tokens` Table**: This table stores information about both revoked access tokens and refresh tokens. When a token is revoked—whether manually or via a logout operation—it is inserted into this table to ensure that no further requests can be authenticated using it.

When a user logs out or when the `/auth/auth_tokens` DELETE endpoint is called, both the access token and the associated refresh token (determined by the JWT subject) are stored in the `revoked_tokens` table. This ensures that any future attempts to use these tokens will be blocked, as the filter will reference the `revoked_tokens` table to reject requests containing revoked tokens.

#### Secure Request Handling

The filter operates behind the scenes for every authenticated request, ensuring a robust security layer by:

- **Checking Token Validity**: The filter checks the expiration date and revocation status of the access token.
- **Cross-Referencing Active Tokens**: It verifies if an active refresh token exists for the user.
- **Proactive Revocation**: If no active refresh token is found, the system revokes the current access token and prevents further use of both tokens.

By integrating the `DefaultJwtValidityFilter`, you can trust that your application’s token-based authentication will rigorously enforce token expiration, revocation, and validation processes, automatically keeping your application secure without requiring additional manual configurations.
