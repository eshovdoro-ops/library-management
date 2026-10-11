package com.library.dto;

import com.library.entity.AppUser;
import com.library.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank @Size(max = 50) String username,
            @NotBlank @Size(max = 72) String password) {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50)
            @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "допустимы латиница, цифры, _ . -") String username,
            @NotBlank @Size(min = 8, max = 72, message = "длина пароля 8–72 символа") String password,
            @NotNull Role role) {}

    public record TokenResponse(String token, String tokenType, long expiresInSeconds, String role) {}

    public record UserResponse(Long id, String username, Role role) {
        public static UserResponse from(AppUser u) { return new UserResponse(u.getId(), u.getUsername(), u.getRole()); }
    }
}
