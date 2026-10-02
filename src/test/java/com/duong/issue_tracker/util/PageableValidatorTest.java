package com.duong.issue_tracker.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PageableValidatorTest {

    @Test
    void shouldAllowDocumentedIssueSortFields() {
        PageRequest pageable = PageRequest.of(0, 20,
                org.springframework.data.domain.Sort.by("priority").descending());

        assertThat(PageableValidator.requireAllowedIssueSort(pageable)).isSameAs(pageable);
    }
}
