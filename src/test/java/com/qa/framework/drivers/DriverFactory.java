package com.qa.framework.drivers;

import com.qa.framework.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.Map;

/**
 * Builds a configured WebDriver from -Dbrowser=chrome|firefox|edge and -DisHeadless=Y|N.
 * Driver binaries are resolved by Selenium Manager (bundled with Selenium 4.6+), so there is no
 * WebDriverManager dependency and no driver path configuration. Stateless: safe from any thread.
 */
public final class DriverFactory {

    private static final Logger LOG = LogManager.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        BrowserType browser = BrowserType.from(ConfigReader.get("browser"));
        boolean headless = ConfigReader.getYesNo("isHeadless");
        LOG.info("Starting {} (headless={})", browser, headless);

        WebDriver driver = switch (browser) {
            case CHROME -> new ChromeDriver(chromeOptions(headless));
            case FIREFOX -> new FirefoxDriver(firefoxOptions(headless));
            case EDGE -> new EdgeDriver(edgeOptions(headless));
        };

        if (headless && driver instanceof ChromiumDriver chromium) {
            useRegularUserAgent(chromium);
        }

        // Implicit wait stays 0: all synchronisation is explicit (WaitUtils). Mixing both causes unpredictable waits.
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("page.load.timeout.seconds")));
        if (!headless) {
            driver.manage().window().maximize();
        }
        return driver;
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new", windowSizeArgument());
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-notifications",
                "--disable-features=PasswordLeakDetection");
        // Stop the password-manager popup from covering the page after login.
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false));
        return options;
    }

    private static EdgeOptions edgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new", windowSizeArgument());
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-notifications");
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless",
                    "--width=" + ConfigReader.get("window.width"),
                    "--height=" + ConfigReader.get("window.height"));
        }
        return options;
    }

    /**
     * Headless Chromium announces itself as "HeadlessChrome" in its user agent, and some sites treat that as a
     * bot and silently ignore interactions (seen in CI: "Add to cart" clicks had no effect, while the same test
     * passed in a headed browser). Swap the token so headless runs behave like a real user's browser.
     * The version number is kept, so the override never goes stale.
     */
    private static void useRegularUserAgent(ChromiumDriver driver) {
        Object userAgent = driver.executeScript("return navigator.userAgent");
        if (userAgent instanceof String ua && ua.contains("HeadlessChrome")) {
            String regular = ua.replace("HeadlessChrome", "Chrome");
            driver.executeCdpCommand("Network.setUserAgentOverride", Map.of("userAgent", regular));
            LOG.info("Headless user agent replaced: {}", regular);
        }
    }

    private static String windowSizeArgument() {
        return "--window-size=" + ConfigReader.get("window.width") + "," + ConfigReader.get("window.height");
    }
}
