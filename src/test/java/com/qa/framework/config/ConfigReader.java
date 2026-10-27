package com.qa.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Read-only configuration.
 * <p>
 * Resolution order for {@link #get(String)} (first match wins):
 * <ol>
 *     <li>JVM system property ({@code -Dkey=value}, forwarded by Maven Surefire)</li>
 *     <li>{@code config-<env>.properties}, where env comes from {@code -Denv} (default {@code qa})</li>
 *     <li>{@code config.properties} (common defaults)</li>
 * </ol>
 * Secrets never live in property files: {@link #secret(String)} reads environment variables only.
 * <p>
 * The merged values are loaded once into an unmodifiable map, so this class holds no mutable state
 * and is safe to call from parallel scenarios.
 */
public final class ConfigReader {

    private static final String COMMON_FILE = "config.properties";
    private static final String DEFAULT_ENV = "qa";
    private static final String ENVIRONMENT =
            System.getProperty("env", DEFAULT_ENV).trim().toLowerCase(Locale.ROOT);
    private static final Map<String, String> FILE_VALUES = loadFileValues();

    private ConfigReader() {
    }

    public static String environment() {
        return ENVIRONMENT;
    }

    /** Required key: fails fast with a clear message when it is missing everywhere. */
    public static String get(String key) {
        return getOptional(key).orElseThrow(() -> new IllegalStateException(
                "Missing configuration key '" + key + "' (env=" + ENVIRONMENT + "). Add it to "
                        + COMMON_FILE + " or config-" + ENVIRONMENT + ".properties, or pass -D" + key + "=..."));
    }

    public static Optional<String> getOptional(String key) {
        String fromSystem = System.getProperty(key);
        if (fromSystem != null && !fromSystem.isBlank()) {
            return Optional.of(fromSystem.trim());
        }
        return Optional.ofNullable(FILE_VALUES.get(key));
    }

    public static int getInt(String key) {
        String value = get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Configuration key '" + key + "' must be an integer but was '" + value + "'", e);
        }
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    /** Accepts Y/N (as used on the command line, e.g. -DisHeadless=Y) as well as true/false. */
    public static boolean getYesNo(String key) {
        String value = get(key).toUpperCase(Locale.ROOT);
        return switch (value) {
            case "Y", "YES", "TRUE" -> true;
            case "N", "NO", "FALSE" -> false;
            default -> throw new IllegalStateException(
                    "Configuration key '" + key + "' must be Y or N but was '" + value + "'");
        };
    }

    /**
     * Secrets come ONLY from environment variables (a local .env file loaded into the shell,
     * an Eclipse run configuration, or CI secrets). They are never read from committed files.
     */
    public static String secret(String environmentVariable) {
        String value = System.getenv(environmentVariable);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Secret '" + environmentVariable + "' is not set. "
                    + "Copy .env.example to .env and load it (set -a; source .env; set +a), "
                    + "or configure it as a CI secret.");
        }
        return value;
    }

    private static Map<String, String> loadFileValues() {
        Map<String, String> merged = new HashMap<>(load(COMMON_FILE));
        merged.putAll(load("config-" + ENVIRONMENT + ".properties"));
        return Map.copyOf(merged);
    }

    private static Map<String, String> load(String resource) {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Configuration file '" + resource
                        + "' not found on the classpath. Supported environments: qa, staging (-Denv=...)");
            }
            Properties properties = new Properties();
            properties.load(in);
            Map<String, String> values = new HashMap<>();
            properties.stringPropertyNames().forEach(name -> values.put(name, properties.getProperty(name).trim()));
            return values;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read configuration file '" + resource + "'", e);
        }
    }
}
