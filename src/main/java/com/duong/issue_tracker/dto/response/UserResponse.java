package com.duong.issue_tracker.dto.response;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String role
) {
}
