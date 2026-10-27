package com.qa.framework.drivers;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum BrowserType {
    CHROME, FIREFOX, EDGE;

    public static BrowserType from(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            String supported = Arrays.stream(values()).map(b -> b.name().toLowerCase(Locale.ROOT))
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Unsupported browser '" + value + "'. Use one of: " + supported, e);
        }
    }
}
