package com.example.security.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationTime;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-time}") long expirationTime,
            @Value("${security.jwt.issuer}") String issuer) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("security.jwt.secret phải có ít nhất 32 byte cho HS256");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationTime = expirationTime;
        this.issuer = issuer;
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime))
                .claim("authorities", authorities)
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        return userDetails.getUsername().equalsIgnoreCase(claims.getSubject())
                && issuer.equals(claims.getIssuer())
                && claims.getExpiration() != null
                && claims.getExpiration().after(new Date());
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}
