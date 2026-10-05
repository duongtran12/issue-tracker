package com.duong.issue_tracker.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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
        LocalDate dueDate,
        Integer estimateMinutes,
        List<LabelResponse> labels,
        Instant createdAt,
        Instant updatedAt
) {
}
