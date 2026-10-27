package com.qa.framework.pages.ui;

import com.qa.framework.drivers.WebDriverWrapper;
import io.cucumber.java.Scenario;
import org.openqa.selenium.By;

import java.util.List;

public class InventoryPage extends BasePage {

    private final By pageTitle = By.cssSelector(".title");
    private final By itemNames = By.cssSelector(".inventory_item_name");
    private final By itemPrices = By.cssSelector(".inventory_item_price");
    private final By sortDropdown = By.cssSelector("[data-test='product-sort-container']");
    private final By cartBadge = By.cssSelector(".shopping_cart_badge");
    private final By cartLink = By.cssSelector(".shopping_cart_link");

    /** The add/remove button inside the product card whose name matches exactly. */
    private static final String ITEM_BUTTON_XPATH =
            "//div[contains(@class,'inventory_item_name') and normalize-space()='%s']"
                    + "/ancestor::div[contains(concat(' ', normalize-space(@class), ' '), ' inventory_item ')]//button";

    public InventoryPage(WebDriverWrapper driver, Scenario scenario) {
        super(driver, scenario);
    }

    public boolean isLoaded() {
        driver.waitForUrlContaining("inventory.html");
        return "Products".equals(driver.text(pageTitle));
    }

    public int productCount() {
        return driver.texts(itemNames).size();
    }

    public void addToCart(String productName) {
        log.info("Adding '{}' to the cart", productName);
        driver.clickUntilText(itemButton(productName), "Remove");
    }

    public void removeFromCart(String productName) {
        log.info("Removing '{}' from the products page", productName);
        driver.clickUntilText(itemButton(productName), "Add to cart");
    }

    /** 0 when the badge is not shown (empty cart). */
    public int cartBadgeCount() {
        List<String> badge = driver.textsIfPresent(cartBadge);
        return badge.isEmpty() ? 0 : Integer.parseInt(badge.get(0));
    }

    public void sortBy(String visibleOption) {
        log.info("Sorting products by '{}'", visibleOption);
        driver.selectByVisibleText(sortDropdown, visibleOption);
    }

    public List<String> productNames() {
        List<String> names = driver.texts(itemNames);
        attachText("product-names", String.join("\n", names));
        return names;
    }

    public List<Double> productPrices() {
        List<String> prices = driver.texts(itemPrices);
        attachText("product-prices", String.join("\n", prices));
        return prices.stream().map(p -> Double.parseDouble(p.replace("$", "").trim())).toList();
    }

    public void openCart() {
        driver.click(cartLink);
    }

    private By itemButton(String productName) {
        return By.xpath(String.format(ITEM_BUTTON_XPATH, productName));
    }
}
