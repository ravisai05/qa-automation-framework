package com.qa.framework.pages.ui;

import com.qa.framework.drivers.WebDriverWrapper;
import io.cucumber.java.Scenario;
import org.openqa.selenium.By;

import java.util.List;

public class CartPage extends BasePage {

    private final By pageTitle = By.cssSelector(".title");
    private final By itemNames = By.cssSelector(".cart_item .inventory_item_name");
    private final By checkoutButton = By.id("checkout");

    private static final String REMOVE_BUTTON_XPATH =
            "//div[contains(@class,'inventory_item_name') and normalize-space()='%s']"
                    + "/ancestor::div[contains(concat(' ', normalize-space(@class), ' '), ' cart_item ')]//button";

    public CartPage(WebDriverWrapper driver, Scenario scenario) {
        super(driver, scenario);
    }

    public boolean isLoaded() {
        driver.waitForUrlContaining("cart.html");
        return "Your Cart".equals(driver.text(pageTitle));
    }

    /** Non-waiting: an empty cart is a valid state. */
    public List<String> itemNames() {
        return driver.textsIfPresent(itemNames);
    }

    public void removeItem(String productName) {
        log.info("Removing '{}' from the cart page", productName);
        By button = By.xpath(String.format(REMOVE_BUTTON_XPATH, productName));
        driver.click(button);
        driver.waitForInvisibility(button);
    }

    public void proceedToCheckout() {
        driver.click(checkoutButton);
    }
}
