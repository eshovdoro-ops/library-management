package com.library.service;

import com.library.dto.AuthDtos.*;
import com.library.entity.AppUser;
import com.library.exception.ConflictException;
import com.library.repository.UserRepository;
import com.library.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    /** Хэш-«пустышка»: проверка пароля выполняется и для несуществующего пользователя (защита от user enumeration по времени). */
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.dummyHash = encoder.encode("dummy-password-for-timing");
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        AppUser user = users.findByUsername(req.username()).orElse(null);
        boolean matches = encoder.matches(req.password(), user != null ? user.getPasswordHash() : dummyHash);
        if (user == null || !matches) {
            throw new BadCredentialsException("Неверное имя пользователя или пароль");
        }
        return new TokenResponse(jwt.generate(user.getUsername(), user.getRole().name()),
                "Bearer", jwt.expiresInSeconds(), user.getRole().name());
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (users.existsByUsername(req.username())) {
            throw new ConflictException("Пользователь с таким именем уже существует");
        }
        AppUser u = new AppUser();
        u.setUsername(req.username());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole(req.role());
        return UserResponse.from(users.save(u));
    }
}
