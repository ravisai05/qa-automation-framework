package com.qa.framework.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/** Run-level switches (parallelism, filtering, retries) with validation and safe fallbacks. */
public final class ExecutionSettings {

    public static final int DEFAULT_THREAD_COUNT = 2;
    /** Upper bound protects shared CI runners from accidentally starting dozens of browsers. */
    public static final int MAX_THREAD_COUNT = 16;

    private static final Logger LOG = LogManager.getLogger(ExecutionSettings.class);

    private ExecutionSettings() {
    }

    /** -DthreadCount: a positive integer up to {@value #MAX_THREAD_COUNT}; anything else falls back to 2. */
    public static int threadCount() {
        String raw = ConfigReader.getOptional("threadCount").orElse(String.valueOf(DEFAULT_THREAD_COUNT));
        try {
            int value = Integer.parseInt(raw.trim());
            if (value >= 1 && value <= MAX_THREAD_COUNT) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // handled by the fallback below
        }
        LOG.warn("Invalid threadCount '{}' (allowed 1..{}). Falling back to {}.", raw, MAX_THREAD_COUNT, DEFAULT_THREAD_COUNT);
        return DEFAULT_THREAD_COUNT;
    }

    /** -DfeatureToRun, e.g. "+TC-UI-001,@smoke". Empty when not supplied. */
    public static Optional<String> featureToRun() {
        return ConfigReader.getOptional("featureToRun");
    }

    /** -DisRetry=true enables retries of NORMAL failures (browser crashes are always retried). */
    public static boolean isRetryEnabled() {
        return ConfigReader.getBoolean("isRetry");
    }

    /** -DretryCount: retries per scenario for normal failures when isRetry=true. Negative values become 0. */
    public static int retryCount() {
        return Math.max(0, ConfigReader.getInt("retryCount"));
    }
}
