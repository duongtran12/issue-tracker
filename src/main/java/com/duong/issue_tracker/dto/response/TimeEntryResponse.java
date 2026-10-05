package com.duong.issue_tracker.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record TimeEntryResponse(
        Long id, Long issueId, Long userId, String username,
        int minutes, LocalDate workDate, String note,
        Instant createdAt, Instant updatedAt
) {
}
