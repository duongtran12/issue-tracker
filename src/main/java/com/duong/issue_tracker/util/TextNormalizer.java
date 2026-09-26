package com.duong.issue_tracker.util;

import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String compact(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    public static String optional(String value) {
        String normalized = compact(value);
        return normalized == null || normalized.isEmpty() ? null : normalized;
    }

    public static String multiline(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\r\n", "\n").replace('\r', '\n').trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public static String username(String value) {
        String normalized = compact(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    public static String email(String value) {
        return username(value);
    }

    public static String projectKey(String value) {
        String normalized = compact(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }
}
