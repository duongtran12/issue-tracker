package com.duong.issue_tracker.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long expiresIn
) {
}
