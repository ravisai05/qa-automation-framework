package com.qa.framework.pages.ui;

import com.qa.framework.config.ConfigReader;
import com.qa.framework.drivers.WebDriverWrapper;
import io.cucumber.java.Scenario;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    private final By usernameInput = By.id("user-name");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By errorMessage = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriverWrapper driver, Scenario scenario) {
        super(driver, scenario);
    }

    public LoginPage open() {
        String url = ConfigReader.get("ui.base.url");
        log.info("Opening {}", url);
        driver.open(url);
        driver.waitForVisible(loginButton);
        return this;
    }

    /** The password is never logged. */
    public void loginAs(String username, String password) {
        log.info("Logging in as '{}'", username);
        driver.type(usernameInput, username);
        driver.type(passwordInput, password);
        driver.click(loginButton);
    }

    public String errorMessage() {
        return driver.text(errorMessage);
    }

    public boolean isLoginButtonDisplayed() {
        return driver.isDisplayed(loginButton);
    }
}
