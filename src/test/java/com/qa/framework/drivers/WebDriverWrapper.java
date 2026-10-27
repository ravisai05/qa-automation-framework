package com.qa.framework.drivers;

import com.qa.framework.config.ConfigReader;
import com.qa.framework.retry.BrowserCrashSignatures;
import com.qa.framework.utils.ScreenshotUtils;
import com.qa.framework.utils.WaitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * The only class pages use to talk to the browser. Every action waits explicitly before acting and runs
 * through {@link #execute(String, Supplier)}, which adds crash recovery:
 * <p>
 * When an action fails with a browser-crash signature (see {@link BrowserCrashSignatures}) the wrapper tries
 * ONE page reload and repeats the action once. That rescues a crashed tab whose session is still alive.
 * If the session itself is dead the reload fails too, the ORIGINAL exception is rethrown, and
 * {@code RetryAnalyzer} re-runs the whole scenario with a fresh browser.
 */
public class WebDriverWrapper {

    private static final Logger LOG = LogManager.getLogger(WebDriverWrapper.class);

    private final WebDriver driver;
    private final WaitUtils waits;

    public WebDriverWrapper(WebDriver driver) {
        this.driver = driver;
        this.waits = new WaitUtils(driver, Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds")));
    }

    public void open(String url) {
        execute("open " + url, () -> {
            driver.get(url);
            return null;
        });
    }

    public void click(By locator) {
        execute("click " + locator, () -> {
            waits.clickable(locator).click();
            return null;
        });
    }

    /** Clears the field, then types the text (an empty or null text leaves the field empty). */
    public void type(By locator, String text) {
        execute("type into " + locator, () -> {
            WebElement element = waits.visible(locator);
            element.clear();
            if (text != null && !text.isEmpty()) {
                element.sendKeys(text);
            }
            return null;
        });
    }

    public String text(By locator) {
        return execute("read text of " + locator, () -> waits.visible(locator).getText().trim());
    }

    public List<String> texts(By locator) {
        return execute("read texts of " + locator,
                () -> waits.allVisible(locator).stream().map(e -> e.getText().trim()).toList());
    }

    /** Non-waiting lookup: returns an empty list when nothing matches (e.g. an absent cart badge). */
    public List<String> textsIfPresent(By locator) {
        return execute("read optional texts of " + locator,
                () -> driver.findElements(locator).stream().map(e -> e.getText().trim()).toList());
    }

    public boolean isDisplayed(By locator) {
        return execute("check display of " + locator,
                () -> driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed));
    }

    public void selectByVisibleText(By locator, String option) {
        execute("select '" + option + "' in " + locator, () -> {
            new Select(waits.visible(locator)).selectByVisibleText(option);
            return null;
        });
    }

    public void waitForVisible(By locator) {
        execute("wait for " + locator + " to be visible", () -> waits.visible(locator));
    }

    public void waitForUrlContaining(String fragment) {
        execute("wait for URL containing " + fragment, () -> {
            waits.urlContains(fragment);
            return null;
        });
    }

    /**
     * Clicks a toggle-style button and confirms its label changed to {@code expectedText}. If the first click
     * was swallowed (e.g. a React handler not yet attached while the page hydrates), clicks ONCE more - only
     * when the label is still unchanged, so the action can never toggle twice - then waits the full timeout.
     */
    public void clickUntilText(By locator, String expectedText) {
        click(locator);
        if (!waits.textBecomesWithin(locator, expectedText, Duration.ofSeconds(3))) {
            LOG.warn("Click on {} did not change its text to '{}' - clicking once more", locator, expectedText);
            click(locator);
        }
        waitForText(locator, expectedText);
    }

    public void waitForText(By locator, String text) {
        execute("wait for text '" + text + "' in " + locator, () -> {
            waits.textToBe(locator, text);
            return null;
        });
    }

    public void waitForInvisibility(By locator) {
        execute("wait for " + locator + " to disappear", () -> {
            waits.invisible(locator);
            return null;
        });
    }

    public String currentUrl() {
        return execute("read current URL", driver::getCurrentUrl);
    }

    public byte[] screenshot() {
        return ScreenshotUtils.capture(driver);
    }

    /** For lifecycle code (DriverManager) only - pages must go through the safe methods above. */
    public WebDriver rawDriver() {
        return driver;
    }

    private <T> T execute(String description, Supplier<T> action) {
        try {
            return action.get();
        } catch (WebDriverException firstFailure) {
            if (!BrowserCrashSignatures.isBrowserCrash(firstFailure)) {
                throw firstFailure;
            }
            LOG.warn("Browser crash detected during '{}': {}. Reloading the page once.",
                    description, BrowserCrashSignatures.firstLine(firstFailure));
            try {
                driver.navigate().refresh();
                return action.get();
            } catch (WebDriverException recoveryFailure) {
                LOG.error("Crash recovery failed for '{}' - handing over to scenario-level retry", description);
                firstFailure.addSuppressed(recoveryFailure);
                throw firstFailure;
            }
        }
    }
}
