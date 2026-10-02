package com.duong.issue_tracker.util;

import org.springframework.data.domain.Pageable;

import java.util.Set;

public final class PageableValidator {

    private static final Set<String> ISSUE_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "title", "status", "priority");

    private PageableValidator() {
    }

    public static Pageable requireAllowedIssueSort(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!ISSUE_SORT_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException(
                        "Unsupported issue sort field: " + order.getProperty());
            }
        });
        return pageable;
    }
}
