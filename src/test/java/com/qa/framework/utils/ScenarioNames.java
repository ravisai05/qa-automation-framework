package com.qa.framework.utils;

import io.cucumber.testng.PickleWrapper;
import org.testng.ITestResult;

import java.util.List;

/** Extracts Cucumber scenario details from a TestNG result of CucumberTestRunner.runScenario. */
public final class ScenarioNames {

    private ScenarioNames() {
    }

    public static String of(ITestResult result) {
        PickleWrapper pickle = pickleOf(result);
        return pickle == null ? result.getName() : pickle.getPickle().getName();
    }

    public static List<String> tagsOf(ITestResult result) {
        PickleWrapper pickle = pickleOf(result);
        return pickle == null ? List.of() : pickle.getPickle().getTags();
    }

    private static PickleWrapper pickleOf(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0 && parameters[0] instanceof PickleWrapper wrapper) {
            return wrapper;
        }
        return null;
    }
}
