package com.duong.issue_tracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank(message = "Project name is required")
        @Size(max = 100, message = "Project name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Project key is required")
        @Size(min = 2, max = 20, message = "Project key must be between 2 and 20 characters")
        String key,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {
}
