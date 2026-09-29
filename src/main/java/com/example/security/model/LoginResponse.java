package com.example.security.model;

public record LoginResponse(String token, long expiresIn, String tokenType) {
}
