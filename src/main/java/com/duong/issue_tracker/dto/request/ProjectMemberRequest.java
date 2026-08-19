package com.duong.issue_tracker.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProjectMemberRequest(
        @NotBlank(message = "Username is required")
        String username
) {
}
