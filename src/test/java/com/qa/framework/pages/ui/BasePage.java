package com.qa.framework.pages.ui;

import com.qa.framework.drivers.WebDriverWrapper;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Pages get the safe {@link WebDriverWrapper} (never the raw WebDriver) and the current Cucumber
 * {@link Scenario}, so they can attach evidence (screenshots, values read) straight into the reports.
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final WebDriverWrapper driver;
    private final Scenario scenario;

    protected BasePage(WebDriverWrapper driver, Scenario scenario) {
        this.driver = driver;
        this.scenario = scenario;
    }

    protected void attachScreenshot(String name) {
        scenario.attach(driver.screenshot(), "image/png", name);
    }

    protected void attachText(String name, String text) {
        scenario.attach(text, "text/plain", name);
    }

    public String currentUrl() {
        return driver.currentUrl();
    }
}
