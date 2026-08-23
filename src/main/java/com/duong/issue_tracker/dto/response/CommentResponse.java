package com.duong.issue_tracker.dto.response;

import java.time.Instant;

public record CommentResponse(
        Long id,
        Long issueId,
        Long authorId,
        String authorUsername,
        String body,
        Instant createdAt,
        Instant updatedAt
) {
}
