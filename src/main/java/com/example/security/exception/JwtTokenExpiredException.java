package com.example.security.exception;

public class JwtTokenExpiredException extends JwtTokenException {

    public JwtTokenExpiredException(String message) {
        super(message);
    }
}
