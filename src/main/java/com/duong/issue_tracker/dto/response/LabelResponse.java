package com.duong.issue_tracker.dto.response;

public record LabelResponse(
        Long id,
        Long projectId,
        String name,
        String color
) {
}
