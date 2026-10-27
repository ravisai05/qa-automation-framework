package com.qa.framework.utils;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

/** Helpers for the traceability tags (@TC-UI-001, @TC-API-001 ...). */
public final class TagUtils {

    private static final String TEST_CASE_PREFIX = "@tc-";

    private TagUtils() {
    }

    /** The scenario's test-case id without '@', e.g. "TC-UI-001". */
    public static Optional<String> testCaseId(Collection<String> tags) {
        return tags.stream()
                .filter(tag -> tag.toLowerCase(Locale.ROOT).startsWith(TEST_CASE_PREFIX))
                .map(tag -> tag.substring(1))
                .findFirst();
    }

    /** Safe for file names and log routing: letters, digits, '-' and '_' only. */
    public static String fileSafe(String text) {
        String cleaned = text.replaceAll("[^A-Za-z0-9_-]+", "_");
        return cleaned.length() > 80 ? cleaned.substring(0, 80) : cleaned;
    }
}
