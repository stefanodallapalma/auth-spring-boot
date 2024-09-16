package io.stefadp.auth.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotBlank
    private String tokenValue = "";

    private Instant expiresAt = Instant.now();

    public RevokedToken() {
    }

    public RevokedToken(String tokenValue, Instant expiresAt) {
        this.tokenValue = tokenValue;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevokedToken that = (RevokedToken) o;
        return Objects.equals(tokenValue, that.tokenValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenValue);
    }

    @Override
    public String toString() {
        return "RevokedToken{" +
                "id=" + id +
                ", tokenValue='" + tokenValue + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}