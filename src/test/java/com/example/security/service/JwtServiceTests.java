package com.example.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.security.exception.JwtTokenException;
import com.example.security.exception.JwtTokenExpiredException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.SignedJWT;

class JwtServiceTests {

    private JwtService jwtService;
    private UserDetails user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "test-secret-key-with-at-least-thirty-two-bytes-2026",
                60_000,
                "jwt-nimbus-test");
        user = User.withUsername("student@example.com")
                .password("encoded-password")
                .roles("USER")
                .build();
    }

    @Test
    void generatedTokenContainsUsernameUsesHs256AndIsValid() throws Exception {
        String token = jwtService.generateToken(user);

        assertEquals("student@example.com", jwtService.extractUsername(token));
        assertEquals(JWSAlgorithm.HS256, SignedJWT.parse(token).getHeader().getAlgorithm());
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void changedSignatureIsRejected() {
        String token = jwtService.generateToken(user);
        char replacement = token.endsWith("a") ? 'b' : 'a';
        String tampered = token.substring(0, token.length() - 1) + replacement;

        assertThrows(JwtTokenException.class, () -> jwtService.extractUsername(tampered));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService expiredTokenService = new JwtService(
                "test-secret-key-with-at-least-thirty-two-bytes-2026",
                -1,
                "jwt-nimbus-test");
        String token = expiredTokenService.generateToken(user);

        assertThrows(JwtTokenExpiredException.class,
                () -> expiredTokenService.extractUsername(token));
    }
}
