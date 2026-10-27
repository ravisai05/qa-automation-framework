package com.qa.framework.pages.ui;

import com.qa.framework.drivers.WebDriverWrapper;
import io.cucumber.java.Scenario;
import org.openqa.selenium.By;

/** Checkout step two: order overview with totals. */
public class CheckoutOverviewPage extends BasePage {

    private final By itemTotalLabel = By.cssSelector(".summary_subtotal_label");
    private final By totalLabel = By.cssSelector(".summary_total_label");
    private final By finishButton = By.id("finish");

    public CheckoutOverviewPage(WebDriverWrapper driver, Scenario scenario) {
        super(driver, scenario);
    }

    public boolean isLoaded() {
        driver.waitForUrlContaining("checkout-step-two.html");
        return driver.isDisplayed(finishButton);
    }

    /** "Item total: $29.99" -> 29.99 */
    public double itemTotal() {
        return amountOf(driver.text(itemTotalLabel));
    }

    /** "Total: $32.39" -> 32.39 */
    public double total() {
        String label = driver.text(totalLabel);
        attachText("order-total", label);
        return amountOf(label);
    }

    public void finish() {
        driver.click(finishButton);
    }

    private static double amountOf(String label) {
        return Double.parseDouble(label.substring(label.indexOf('$') + 1).trim());
    }
}
