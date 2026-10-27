package com.qa.framework.managers;

import com.qa.framework.drivers.DriverFactory;
import com.qa.framework.drivers.WebDriverWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

/**
 * Owns the browser of ONE scenario. No ThreadLocal is needed: PicoContainer creates a new DriverManager per
 * scenario and a scenario never changes thread, so the plain field is already isolated.
 * API-only scenarios never call {@link #start()}, so they never open a browser.
 */
public class DriverManager {

    private static final Logger LOG = LogManager.getLogger(DriverManager.class);

    private WebDriverWrapper wrapper;

    public void start() {
        if (wrapper != null) {
            throw new IllegalStateException("Browser already started for this scenario");
        }
        wrapper = new WebDriverWrapper(DriverFactory.createDriver());
    }

    public boolean isStarted() {
        return wrapper != null;
    }

    public WebDriverWrapper wrapper() {
        if (wrapper == null) {
            throw new IllegalStateException("No browser for this scenario. UI steps need the @ui tag "
                    + "(Hooks starts the browser only for @ui scenarios).");
        }
        return wrapper;
    }

    public void quit() {
        if (wrapper == null) {
            return;
        }
        WebDriver driver = wrapper.rawDriver();
        wrapper = null;
        try {
            driver.quit();
        } catch (WebDriverException e) {
            // A crashed browser often cannot be quit cleanly; the session is gone either way.
            LOG.warn("Browser did not quit cleanly: {}", e.getMessage());
        }
    }
}
