package com.duong.issue_tracker.dto.request;

import com.duong.issue_tracker.enums.IssuePriority;
import com.duong.issue_tracker.enums.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IssueRequest(
        @NotBlank(message = "Issue title is required")
        @Size(max = 200, message = "Issue title must not exceed 200 characters")
        String title,

        @Size(max = 5000, message = "Issue description must not exceed 5000 characters")
        String description,

        IssueStatus status,
        IssuePriority priority,
        String assigneeUsername
) {
}
