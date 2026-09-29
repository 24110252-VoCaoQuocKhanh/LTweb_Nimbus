package com.example.security.model;

import java.time.LocalDateTime;

import com.example.security.entity.User;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
