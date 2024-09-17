# Authentication and Token Management Library


This library provides a robust solution for handling JWT-based authentication, including the creation, management, and revocation of access and refresh tokens. It is designed to facilitate secure user authentication workflows, including token rotation, revocation, and refresh operations.
It comes with an out-of-the-box controller to handle CRUD operations for auth token management.

![Alt Text](./docs/preview.gif)



## Features

- **JWT Token Generation**: Create JSON Web Tokens (JWT) for authenticated users.
- **Refresh Token Management**: Securely generate, store, and rotate refresh tokens.
- **Token Revocation**: Mark tokens as revoked to prevent further use.
- **Integrated Token Facade**: Simplify token operations through a unified interface.
- **Security Compliance**: Ensure that tokens are securely managed and rotated as per best practices.


## Table of Contents
- [Requirements](#requirements)
- [Installation](#installation)
- [Getting Started](#getting-started)
    - [Scenarios to Consider](#scenarios-to-consider)
    - [Configure JWT Settings](#configure-jwt-settings)
    - [(Optional) Configure Refresh Token Settings](#configure-refresh-token-settings)
    - [Integrate JWT and Refresh Token with Your Application](#integrate-jwt-and-refresh-token-with-your-application)
- [Key Components](#key-components)
    - [AuthTokensManagementFacade](#authtokensmanagementfacade)
    - [JwtTokenService](#jwttokenservice)
    - [RefreshTokenManagementService](#refreshtokenmanagementservice)
    - [TokenRevocationService](#tokenrevocationservice)
    - [DefaultJwtValidityFilter](#defaultjwtvalidityfilter)
- [Usage Notes](#usage-notes)


## Requirements

To use this library, your project must meet the following requirements:

- **Java 17 or higher**.
- **Spring Boot 3.x**.
- **Dependencies**:
    - `org.springframework.boot:spring-boot-starter-security`
    - `org.springframework.boot:spring-boot-starter-web`
    - `org.springframework.boot:spring-boot-starter-data-jpa`
    - `org.springdoc:springdoc-openapi-starter-webmvc-ui`

## Installation



## Getting Started

## 1. Add library

First, add the Library to your project:

- If you have the library JAR, place it in the `libs` directory of your project.
- Update your `build.gradle` to include the library:

   ```gradle
   dependencies {
       implementation files('libs/auth-spring-boot-starter-0.0.1.jar')
   }
   ```
   
## 2. Configure JWT Settings

Then, create a configuration class to define how the JWT tokens are generated, including expiration time, issuer, and the signing algorithm.

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
                .defaultIssuer() // self
                .build();
    }
}
```
<details>
<summary>Notes</summary>

* _The `JWT_SECRET` should be securely managed and not hardcoded in production environments. **Use environment variables or secure vaults to manage sensitive information.**_

* _The `JWT_SECRET` should be a secure, random string. It can be a Base64-encoded string, a hexadecimal string, or a plain alphanumeric string. Ensure that the SECRET is long enough and suitably complex for the selected signing algorithm. For `HS512`, a 64-byte (512-bit) secret is recommended._
</details>


## 3. (Optional) Configure Refresh Token Settings

<details>
Next, create a configuration class for Refresh Tokens. This class will handle the generation, encoding, and expiration settings for refresh tokens.

```java
@Configuration
public class RefreshTokenConfiguration {

    @Bean // Optional: only if you want to replace the default one below
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // Optional: only if you want to replace the default one below
    public RefreshTokenGenerator refreshTokenGenerator() {
        return new SecureRandomRefreshTokenGenerator(new SecureRandom(), 16);
    }

    @Bean // Optional: only if you want to replace the default one below
    public RefreshTokenSettings refreshTokenSettings() {
        return new RefreshTokenSettings.Builder()
                .expirationTime(7) // Set the refresh token expiration time (default is 7)
                .expirationTimeUnit(ChronoUnit.DAYS) // Set the time unit (default is DAYS)
                .build();
    }
}
```

### Explanation:

* **PasswordEncoder:** Encrypts the refresh token for secure storage. The default implementation uses BCrypt, but you can customize it.
* **RefreshTokenGenerator:** Generates secure random tokens. The default generator uses SecureRandom with 16 bytes length.

</details>


## 4. Integrate JWT and Refresh Token with Your Application

Your application can seamlessly handle authentication token management by utilizing the pre-built endpoints provided by the library through the `AuthTokensController`. This controller exposes endpoints for refreshing tokens and deleting tokens (i.e., logging out). 
This means you do not need to manually implement these endpoints in your own controllers.

### Usage Scenarios:

- **Creating Tokens**: While you still need to implement an endpoint to handle the initial login and creation of JWT and refresh tokens, the `AuthTokensManagementFacade` can be used to simplify this process.

- **Refreshing Tokens**: The `AuthTokensController` already provides endpoints for refreshing both access and refresh tokens (PUT `/auth/auth_tokens`) or refreshing only the access token (PUT `/auth/access_token`). You can simply call these endpoints from your frontend or client applications.

- **Deleting Tokens**: To handle user logout and invalidate both the access and refresh tokens, you can use the `/auth/auth_tokens` DELETE endpoint provided by `AuthTokensController`.

### Example Usage for Login:

While the library handles token management endpoints, you still need to create an endpoint for user login.

```java
@RestController
@RequestMapping("/auth")
public class MyAuthController {

    private final AuthTokensManagementFacade authTokensFacade;

    @Autowired
    public MyAuthController(AuthTokensManagementFacade authTokensFacade) {
        this.authTokensFacade = authTokensFacade;
    }

    @PostMapping("/login") // Or /auth_tokens for consistency
    public ResponseEntity<AuthTokens> login(@RequestBody LoginRequest loginRequest) {
        // Authenticate user based on your use case
        // Then, generate a new pair of JWT access and refresh tokens
        AuthTokens authTokens = authTokensFacade.createAuthTokens(loginRequest.getUsername());
        return ResponseEntity.ok(authTokens);
    }
}
```

### Key Notes:

- The provided `AuthTokensController` handles token refresh and deletion, so there's no need to re-implement these features.
- The `AuthTokensManagementFacade` is available to simplify the creation of tokens during the login process.

TODO: Need to mention that the endpoints should be made accessible in the SecurityConfig.

## Key Components

<details>
  <summary>AuthTokensManagementFacade</summary>

The `AuthTokensManagementFacade` is the primary interface for managing authentication tokens. It provides methods for creating, refreshing, and deleting tokens, encapsulating all the necessary logic to securely handle authentication workflows.

- **Create Auth Tokens**: Generates a new pair of access and refresh tokens.
- **Refresh Auth Tokens**: Rotates both tokens if the refresh token is valid.
- **Refresh Access Token**: Refreshes only the access token, keeping the refresh token unchanged.
- **Delete Auth Tokens**: Revokes both tokens, effectively logging the user out.

#### Scenarios to Consider

1. **Initial Login (Username and Password) → `AuthTokensManagementFacade::createAuthTokens`**
    - **Use Case**: When a user initially logs in with their username and password.
    - **Action**: The `createAuthTokens` method is called to generate a new pair of access and refresh tokens. This is the standard procedure when the user is authenticating with their credentials.

2. **Refreshing Tokens with Token Rotation (Using a Refresh Token) → `AuthTokensManagementFacade::refreshAuthTokens`**
    - **Use Case**: When the user’s access token has expired, but they still have a valid refresh token.
    - **Action**: The `refreshAuthTokens` method is called to generate a new pair of access and refresh tokens using the existing refresh token. This does not involve the user entering their username and password again.
    - **Important Distinction**: This method is not used for re-authenticating with credentials but for extending the session by renewing tokens.

3. **Refreshing Only the Access Token (No Token Rotation) → `AuthTokensManagementFacade::refreshAccessToken`**
    - **Use Case**: When the user wants to refresh only the access token using an existing refresh token. For example, this could be part of a continuous session where only the access token needs to be refreshed without altering the refresh token.
    - **Action**: The `refreshAccessToken` method generates a new access token while leaving the refresh token unchanged. Note, this is less secure than scenario number 2, where also the refresh token gets refreshed. The user should store the refresh token in a safe place. However, the refresh token will still expire according to the user configuration.

</details>


<details>
  <summary>JwtTokenService</summary>

The `JwtTokenService` is responsible for handling the creation and validation of JWT tokens. It interacts with the refresh token store to support token refresh operations.

- **Create**: Generates a new JWT for a given subject.
- **Refresh**: Validates and refreshes a JWT based on a provided refresh token.
- **Get Subject**: Extracts the subject from a JWT.
- **Get Expiry Time**: Retrieves the expiration time of a JWT.
</details>

<details>
  <summary>RefreshTokenManagementService</summary>

The `RefreshTokenManagementService` handles the lifecycle of refresh tokens, including their creation, validation, and invalidation.

- **Create**: Generates and stores a new refresh token for a subject.
- **Refresh**: Rotates the refresh token by invalidating the old one and creating a new one.
- **Invalidate**: Deletes refresh tokens by their value or subject.
</details>


<details>
  <summary>TokenRevocationService</summary>

The `TokenRevocationService` manages the revocation of access tokens. It marks tokens as revoked and checks if a given token has been revoked.

- **Revoke Token**: Marks an access token as revoked.
- **Is Token Revoked**: Checks if an access token has been revoked.

</details>


<details>
  <summary>DefaultJwtValidityFilter</summary>

The `DefaultJwtValidityFilter` is a built-in `OncePerRequestFilter` that automatically validates JWT tokens and checks for related refresh token revocation in every incoming request. This filter ensures that:

- **Token Expiration**: The JWT is checked for expiration. If expired, the request is rejected with an unauthorized status.
- **Token Revocation**: The JWT is checked for revocation status. If revoked, the request is rejected with an unauthorized status.
- **Active Refresh Token Check**: The filter ensures that the user associated with the JWT has an active refresh token. If no active refresh token is found, the JWT is proactively revoked, and the request is rejected.

The `DefaultJwtValidityFilter` is automatically applied in the security configuration and handles the security checks for every request that requires authentication.
</details>
