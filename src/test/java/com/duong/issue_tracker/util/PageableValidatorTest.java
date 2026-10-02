package com.duong.issue_tracker.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageableValidatorTest {

    @Test
    void shouldAllowDocumentedIssueSortFields() {
        PageRequest pageable = PageRequest.of(0, 20,
                org.springframework.data.domain.Sort.by("priority").descending());

        assertThat(PageableValidator.requireAllowedIssueSort(pageable)).isSameAs(pageable);
    }

    @Test
    void shouldRejectUnknownIssueSortField() {
        PageRequest pageable = PageRequest.of(0, 20,
                org.springframework.data.domain.Sort.by("project.owner.password"));

        assertThatThrownBy(() -> PageableValidator.requireAllowedIssueSort(pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported issue sort field");
    }
}
