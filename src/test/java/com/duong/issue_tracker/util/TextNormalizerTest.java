package com.duong.issue_tracker.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextNormalizerTest {

    @Test
    void compactsWhitespace() {
        assertThat(TextNormalizer.compact("  hello\t  world  ")).isEqualTo("hello world");
    }

    @Test
    void convertsBlankOptionalTextToNull() {
        assertThat(TextNormalizer.optional(" \n ")).isNull();
    }

    @Test
    void canonicalizesIdentityValues() {
        assertThat(TextNormalizer.username("  Alice ")).isEqualTo("alice");
        assertThat(TextNormalizer.email(" Alice@Example.COM ")).isEqualTo("alice@example.com");
        assertThat(TextNormalizer.projectKey(" web ")).isEqualTo("WEB");
    }

    @Test
    void preservesIntentionalLineBreaks() {
        assertThat(TextNormalizer.multiline("  first\r\nsecond  ")).isEqualTo("first\nsecond");
    }
}
