package com.duong.issue_tracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "Comment body is required")
        @Size(max = 5000, message = "Comment must not exceed 5000 characters")
        String body
) {
}
