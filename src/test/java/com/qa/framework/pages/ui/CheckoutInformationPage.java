package com.qa.framework.pages.ui;

import com.qa.framework.data.CheckoutCustomer;
import com.qa.framework.drivers.WebDriverWrapper;
import io.cucumber.java.Scenario;
import org.openqa.selenium.By;

/** Checkout step one: customer information. */
public class CheckoutInformationPage extends BasePage {

    private final By firstNameInput = By.id("first-name");
    private final By lastNameInput = By.id("last-name");
    private final By postalCodeInput = By.id("postal-code");
    private final By continueButton = By.id("continue");
    private final By errorMessage = By.cssSelector("[data-test='error']");

    public CheckoutInformationPage(WebDriverWrapper driver, Scenario scenario) {
        super(driver, scenario);
    }

    public boolean isLoaded() {
        driver.waitForUrlContaining("checkout-step-one.html");
        return driver.isDisplayed(continueButton);
    }

    public void submit(CheckoutCustomer customer) {
        log.info("Submitting checkout details {}", customer);
        driver.type(firstNameInput, customer.firstName());
        driver.type(lastNameInput, customer.lastName());
        driver.type(postalCodeInput, customer.postalCode());
        driver.click(continueButton);
    }

    public String errorMessage() {
        return driver.text(errorMessage);
    }
}
