package com.qa.framework.hooks;

import com.qa.framework.context.TestContext;
import com.qa.framework.managers.DriverManager;
import com.qa.framework.utils.TagUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.WebDriverException;

/**
 * Scenario lifecycle. Cucumber runs @Before hooks in ascending order and @After hooks in DESCENDING order,
 * so the browser is started after logging is set up and is closed before logging is torn down.
 */
public class Hooks {

    private static final Logger LOG = LogManager.getLogger(Hooks.class);
    private static final String LOG_KEY = "scenarioId";

    private final TestContext context;

    public Hooks(TestContext context) {
        this.context = context;
    }

    @Before(order = 0)
    public void startScenario(Scenario scenario) {
        context.setScenario(scenario);
        String testCaseId = TagUtils.testCaseId(scenario.getSourceTagNames()).orElse("NO-TC-ID");
        // Routes every log line of this scenario (this thread) to target/logs/scenarios/<id>.log
        ThreadContext.put(LOG_KEY, TagUtils.fileSafe(testCaseId + "_" + scenario.getName() + "_line" + scenario.getLine()));
        LOG.info("START {} | {} | tags={}", testCaseId, scenario.getName(), scenario.getSourceTagNames());
    }

    @Before(value = "@ui", order = 10)
    public void startBrowser() {
        context.driverManager().start();
    }

    @After(value = "@ui", order = 10)
    public void captureEvidenceAndCloseBrowser(Scenario scenario) {
        DriverManager driverManager = context.driverManager();
        try {
            if (scenario.isFailed() && driverManager.isStarted()) {
                scenario.attach(driverManager.wrapper().screenshot(), "image/png", "failure-screenshot");
                scenario.attach(driverManager.wrapper().currentUrl(), "text/plain", "url-at-failure");
            }
        } catch (WebDriverException e) {
            // A crashed browser cannot take screenshots; the original failure is what matters.
            LOG.warn("Could not capture failure evidence: {}", e.getMessage());
        } finally {
            driverManager.quit();
        }
    }

    @After(order = 0)
    public void endScenario(Scenario scenario) {
        LOG.info("END {} -> {}", scenario.getName(), scenario.getStatus());
        ThreadContext.remove(LOG_KEY);
    }
}
