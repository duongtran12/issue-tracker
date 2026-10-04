package com.duong.issue_tracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ChecklistItemRequest(
        @NotBlank(message = "Checklist content is required")
        @Size(max = 500, message = "Checklist content must not exceed 500 characters")
        String content,

        Boolean completed,

        @PositiveOrZero(message = "Checklist position must not be negative")
        Integer position
) {
}
