package com.qa.framework.retry;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Curated, deliberately NARROW list of messages that mean "the browser/session died", not "the app is broken".
 * <p>
 * Trade-off: a broad signature such as "timeout", "no such element" or plain "WebDriverException" would make
 * real product defects and genuine flakiness look green after a retry. Only add a signature here after seeing
 * it in real crash logs, and never one that an application bug could also produce.
 */
public final class BrowserCrashSignatures {

    private static final List<String> SIGNATURES = List.of(
            "invalid session id",
            "session deleted because of page crash",
            "tab crashed",
            "chrome not reachable",
            "devtoolsactiveport");

    /** Guards against pathological cause chains. */
    private static final int MAX_CAUSE_DEPTH = 15;

    private BrowserCrashSignatures() {
    }

    /** Walks the whole cause chain (and suppressed exceptions), so wrapped Selenium errors are still detected. */
    public static boolean isBrowserCrash(Throwable throwable) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        return matches(throwable, visited, 0);
    }

    /** First line of the throwable's message, for logs and the failure summary. */
    public static String firstLine(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getName();
        }
        return message.lines().findFirst().orElse(message).trim();
    }

    private static boolean matches(Throwable current, Set<Throwable> visited, int depth) {
        if (current == null || depth > MAX_CAUSE_DEPTH || !visited.add(current)) {
            return false;
        }
        String message = current.getMessage();
        if (message != null) {
            String lower = message.toLowerCase(Locale.ROOT);
            if (SIGNATURES.stream().anyMatch(lower::contains)) {
                return true;
            }
        }
        for (Throwable suppressed : current.getSuppressed()) {
            if (matches(suppressed, visited, depth + 1)) {
                return true;
            }
        }
        return matches(current.getCause(), visited, depth + 1);
    }
}
