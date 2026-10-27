package com.qa.framework.context;

import com.qa.framework.managers.DriverManager;
import com.qa.framework.managers.PageObjectManager;
import io.cucumber.java.Scenario;

/**
 * Root of the per-scenario object graph. PicoContainer builds one TestContext (and one ScenarioContext and
 * DriverManager) for every scenario and injects the same instances into every step, hook and flow class of
 * that scenario. Nothing here is static, which is what makes parallel scenarios safe.
 */
public class TestContext {

    private final ScenarioContext scenarioContext;
    private final DriverManager driverManager;
    private Scenario scenario;
    private PageObjectManager pageObjectManager;

    public TestContext(ScenarioContext scenarioContext, DriverManager driverManager) {
        this.scenarioContext = scenarioContext;
        this.driverManager = driverManager;
    }

    public ScenarioContext scenarioContext() {
        return scenarioContext;
    }

    public DriverManager driverManager() {
        return driverManager;
    }

    /** Called by the first @Before hook so pages and API filters can attach evidence to the report. */
    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public Scenario scenario() {
        if (scenario == null) {
            throw new IllegalStateException("Scenario not set yet - Hooks.startScenario has not run");
        }
        return scenario;
    }

    /** Page objects for this scenario, created on first use (after the @ui hook started the browser). */
    public PageObjectManager pages() {
        if (pageObjectManager == null) {
            pageObjectManager = new PageObjectManager(driverManager.wrapper(), scenario());
        }
        return pageObjectManager;
    }
}
