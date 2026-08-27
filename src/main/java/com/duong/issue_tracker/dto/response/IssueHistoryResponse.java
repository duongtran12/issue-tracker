package com.duong.issue_tracker.dto.response;

import java.time.Instant;

public record IssueHistoryResponse(
        Long id,
        Long issueId,
        Long actorId,
        String actorUsername,
        String eventType,
        String fieldName,
        String oldValue,
        String newValue,
        Instant createdAt
) {
}
