package io.stefadp.auth.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "revoked_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotBlank
    private String tokenValue = "";

    private Instant expiresAt = Instant.now();

    @Column(unique = true)
    @NotBlank
    private String subject;

    public RefreshToken() {
    }

    public RefreshToken(String tokenValue, Instant expiresAt, String subject) {
        this.tokenValue = tokenValue;
        this.expiresAt = expiresAt;
        this.subject = subject;
    }

    public long getId() {
        return id;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getSubject() {
        return subject;
    }

    public boolean hasExpired() {
        return !hasNotExpired();
    }

    public boolean hasNotExpired() {
        return expiresAt.isAfter(Instant.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(tokenValue, that.tokenValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenValue);
    }
}
