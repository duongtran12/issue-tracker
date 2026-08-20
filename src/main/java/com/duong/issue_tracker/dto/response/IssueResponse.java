package com.duong.issue_tracker.dto.response;

import java.time.Instant;

public record IssueResponse(
        Long id,
        Long projectId,
        String title,
        String description,
        String status,
        String priority,
        Long reporterId,
        String reporterUsername,
        Long assigneeId,
        String assigneeUsername,
        Instant createdAt,
        Instant updatedAt
) {
}
