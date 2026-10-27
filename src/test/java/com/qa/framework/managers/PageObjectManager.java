package com.qa.framework.managers;

import com.qa.framework.drivers.WebDriverWrapper;
import com.qa.framework.pages.ui.CartPage;
import com.qa.framework.pages.ui.CheckoutCompletePage;
import com.qa.framework.pages.ui.CheckoutInformationPage;
import com.qa.framework.pages.ui.CheckoutOverviewPage;
import com.qa.framework.pages.ui.InventoryPage;
import com.qa.framework.pages.ui.LoginPage;
import io.cucumber.java.Scenario;

/**
 * Lazily creates each page once per scenario. Owned by {@code TestContext} (one per scenario), deliberately
 * NOT a global singleton - a singleton would share pages and drivers across parallel threads.
 */
public class PageObjectManager {

    private final WebDriverWrapper driver;
    private final Scenario scenario;

    private LoginPage loginPage;
    private InventoryPage inventoryPage;
    private CartPage cartPage;
    private CheckoutInformationPage checkoutInformationPage;
    private CheckoutOverviewPage checkoutOverviewPage;
    private CheckoutCompletePage checkoutCompletePage;

    public PageObjectManager(WebDriverWrapper driver, Scenario scenario) {
        this.driver = driver;
        this.scenario = scenario;
    }

    public LoginPage loginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(driver, scenario);
        }
        return loginPage;
    }

    public InventoryPage inventoryPage() {
        if (inventoryPage == null) {
            inventoryPage = new InventoryPage(driver, scenario);
        }
        return inventoryPage;
    }

    public CartPage cartPage() {
        if (cartPage == null) {
            cartPage = new CartPage(driver, scenario);
        }
        return cartPage;
    }

    public CheckoutInformationPage checkoutInformationPage() {
        if (checkoutInformationPage == null) {
            checkoutInformationPage = new CheckoutInformationPage(driver, scenario);
        }
        return checkoutInformationPage;
    }

    public CheckoutOverviewPage checkoutOverviewPage() {
        if (checkoutOverviewPage == null) {
            checkoutOverviewPage = new CheckoutOverviewPage(driver, scenario);
        }
        return checkoutOverviewPage;
    }

    public CheckoutCompletePage checkoutCompletePage() {
        if (checkoutCompletePage == null) {
            checkoutCompletePage = new CheckoutCompletePage(driver, scenario);
        }
        return checkoutCompletePage;
    }
}
