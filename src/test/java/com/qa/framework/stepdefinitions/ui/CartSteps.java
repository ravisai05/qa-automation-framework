package com.qa.framework.stepdefinitions.ui;

import com.qa.framework.context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.List;

public class CartSteps {

    private final TestContext context;

    public CartSteps(TestContext context) {
        this.context = context;
    }

    @When("the user adds {string} to the cart")
    public void theUserAddsToTheCart(String product) {
        context.pages().inventoryPage().addToCart(product);
    }

    @When("the user adds the following products to the cart:")
    public void theUserAddsTheFollowingProducts(List<String> products) {
        products.forEach(context.pages().inventoryPage()::addToCart);
    }

    @When("the user removes {string} from the products page")
    public void theUserRemovesFromTheProductsPage(String product) {
        context.pages().inventoryPage().removeFromCart(product);
    }

    @When("the user opens the cart")
    public void theUserOpensTheCart() {
        context.pages().inventoryPage().openCart();
        Assert.assertTrue(context.pages().cartPage().isLoaded(), "Cart page did not load");
    }

    @When("the user removes {string} from the cart page")
    public void theUserRemovesFromTheCartPage(String product) {
        context.pages().cartPage().removeItem(product);
    }

    @Then("the cart badge shows {int}")
    public void theCartBadgeShows(int expected) {
        Assert.assertEquals(context.pages().inventoryPage().cartBadgeCount(), expected, "Unexpected cart badge count");
    }

    @Then("the cart badge is not displayed")
    public void theCartBadgeIsNotDisplayed() {
        Assert.assertEquals(context.pages().inventoryPage().cartBadgeCount(), 0, "Cart badge should not be shown");
    }

    @Then("the cart contains {string}")
    public void theCartContains(String product) {
        theUserOpensTheCart();
        Assert.assertTrue(context.pages().cartPage().itemNames().contains(product),
                "Cart does not contain '" + product + "'");
    }

    @Then("the cart contains only {string}")
    public void theCartContainsOnly(String product) {
        Assert.assertEquals(context.pages().cartPage().itemNames(), List.of(product));
    }
}
