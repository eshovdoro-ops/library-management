package com.library;

import com.library.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-only-secret-with-at-least-32-characters!";

    @Test
    void generateAndParse() {
        JwtService jwt = new JwtService(SECRET, 5);
        assertEquals("alice", jwt.extractUsername(jwt.generate("alice", "ADMIN")));
    }

    @Test
    void shortSecretRejected() {
        assertThrows(IllegalStateException.class, () -> new JwtService("short", 5));
    }

    @Test
    void tamperedTokenRejected() {
        JwtService jwt = new JwtService(SECRET, 5);
        String token = jwt.generate("alice", "ADMIN");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");
        assertThrows(JwtException.class, () -> jwt.extractUsername(tampered));
    }

    @Test
    void tokenSignedWithOtherKeyRejected() {
        JwtService a = new JwtService(SECRET, 5);
        JwtService b = new JwtService("another-secret-with-at-least-32-chars!!!", 5);
        assertThrows(JwtException.class, () -> b.extractUsername(a.generate("alice", "ADMIN")));
    }
}
