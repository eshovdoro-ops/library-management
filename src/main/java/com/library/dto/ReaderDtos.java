package com.library.dto;

import com.library.entity.Reader;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class ReaderDtos {
    private ReaderDtos() {}

    public record Request(
            @NotBlank @Size(max = 255) String fullName,
            @NotBlank @Email @Size(max = 255) String email,
            @Size(max = 30) @Pattern(regexp = "^[0-9+()\\- ]*$", message = "телефон: цифры, + ( ) - и пробел") String phone) {}

    public record Response(Long id, String fullName, String email, String phone, LocalDate registeredAt) {
        public static Response from(Reader r) {
            return new Response(r.getId(), r.getFullName(), r.getEmail(), r.getPhone(), r.getRegisteredAt());
        }
    }
}
