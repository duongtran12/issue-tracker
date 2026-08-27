package com.duong.issue_tracker.dto.response;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        String key,
        String description,
        Long ownerId,
        String ownerUsername,
        Instant createdAt,
        Instant updatedAt
) {
}
