package com.qa.framework.context;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Key/value store for one scenario. PicoContainer creates a fresh instance per scenario, so values never
 * leak between scenarios or threads. A scenario runs on a single thread, so no synchronisation is needed.
 */
public class ScenarioContext {

    private final Map<ContextKey, Object> values = new EnumMap<>(ContextKey.class);

    public void set(ContextKey key, Object value) {
        values.put(key, Objects.requireNonNull(value, () -> "Refusing to store null for " + key));
    }

    public <T> T get(ContextKey key, Class<T> type) {
        Object value = values.get(key);
        if (value == null) {
            throw new IllegalStateException("Nothing stored for " + key
                    + " in this scenario - is a Given step missing?");
        }
        if (!type.isInstance(value)) {
            throw new IllegalStateException(key + " holds a " + value.getClass().getSimpleName()
                    + ", not a " + type.getSimpleName());
        }
        return type.cast(value);
    }

    public int getInt(ContextKey key) {
        return get(key, Integer.class);
    }

    public double getDouble(ContextKey key) {
        return get(key, Double.class);
    }
}
