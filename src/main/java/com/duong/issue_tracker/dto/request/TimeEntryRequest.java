package com.duong.issue_tracker.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TimeEntryRequest(
        @NotNull(message = "Time entry minutes are required")
        @Positive(message = "Time entry minutes must be greater than zero")
        Integer minutes,
        @NotNull(message = "Work date is required")
        @PastOrPresent(message = "Work date cannot be in the future")
        LocalDate workDate,
        @Size(max = 1000, message = "Time entry note must not exceed 1000 characters")
        String note
) {
}
