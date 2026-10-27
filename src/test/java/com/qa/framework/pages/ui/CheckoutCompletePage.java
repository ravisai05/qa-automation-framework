package com.qa.framework.pages.ui;

import com.qa.framework.drivers.WebDriverWrapper;
import io.cucumber.java.Scenario;
import org.openqa.selenium.By;

/** Order confirmation. */
public class CheckoutCompletePage extends BasePage {

    private final By completeHeader = By.cssSelector(".complete-header");

    public CheckoutCompletePage(WebDriverWrapper driver, Scenario scenario) {
        super(driver, scenario);
    }

    public String confirmationHeader() {
        driver.waitForUrlContaining("checkout-complete.html");
        String header = driver.text(completeHeader);
        attachScreenshot("order-confirmation");
        return header;
    }
}
