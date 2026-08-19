package com.duong.issue_tracker.dto.response;

public record ProjectMemberResponse(
        Long userId,
        String username,
        String fullName,
        String role
) {
}
