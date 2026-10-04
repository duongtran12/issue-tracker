package com.duong.issue_tracker.dto.response;

import java.time.Instant;

public record ChecklistItemResponse(
        Long id,
        Long issueId,
        String content,
        boolean completed,
        int position,
        Instant createdAt,
        Instant updatedAt
) {
}
