package com.example.security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.entity.User;
import com.example.security.model.LoginRequest;
import com.example.security.model.LoginResponse;
import com.example.security.model.RegisterRequest;
import com.example.security.model.UserResponse;
import com.example.security.service.AuthenticationService;
import com.example.security.service.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthenticationController(
            AuthenticationService authenticationService,
            JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    @PostMapping({"/signup", "/register"})
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User registeredUser = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(registeredUser));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        User authenticatedUser = authenticationService.authenticate(request);
        String jwt = jwtService.generateToken(authenticatedUser);
        return new LoginResponse(jwt, jwtService.getExpirationTime(), "Bearer");
    }
}
