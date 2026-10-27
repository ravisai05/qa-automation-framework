package com.qa.framework.runner;

import io.cucumber.testng.PickleWrapper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filters the scenario rows produced by TestNGCucumberRunner using a -DfeatureToRun value.
 * <ul>
 *     <li>Comma-separated tags, OR semantics: "+TC-UI-001,+TC-API-002" runs both scenarios.</li>
 *     <li>Each token may start with '+' or '@' (or neither); matching is case-insensitive.</li>
 *     <li>De-duplicated: a scenario matching several tokens is returned once (keyed on feature URI + line).</li>
 * </ul>
 */
public final class ScenarioFilter {

    private final Set<String> wantedTags;

    private ScenarioFilter(Set<String> wantedTags) {
        this.wantedTags = wantedTags;
    }

    public static ScenarioFilter parse(String featureToRun) {
        Set<String> tags = Arrays.stream(featureToRun.split(","))
                .map(ScenarioFilter::normalise)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (tags.isEmpty()) {
            throw new IllegalArgumentException("-DfeatureToRun='" + featureToRun + "' contains no tags. "
                    + "Example: -DfeatureToRun=\"+TC-UI-001,+smoke\"");
        }
        return new ScenarioFilter(tags);
    }

    public Set<String> wantedTags() {
        return wantedTags;
    }

    /** @param rows rows of {PickleWrapper, FeatureWrapper} as returned by provideScenarios() */
    public Object[][] apply(Object[][] rows) {
        Map<String, Object[]> unique = new LinkedHashMap<>();
        for (Object[] row : rows) {
            PickleWrapper pickle = (PickleWrapper) row[0];
            if (matches(pickle.getPickle().getTags())) {
                String key = pickle.getPickle().getUri() + ":" + pickle.getPickle().getLine();
                unique.putIfAbsent(key, row);
            }
        }
        return unique.values().toArray(new Object[0][]);
    }

    private boolean matches(List<String> scenarioTags) {
        return scenarioTags.stream().map(ScenarioFilter::normalise).anyMatch(wantedTags::contains);
    }

    private static String normalise(String token) {
        String trimmed = token.trim();
        if (trimmed.startsWith("+") || trimmed.startsWith("@")) {
            trimmed = trimmed.substring(1).trim();
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
