package com.qa.framework.stepdefinitions.ui;

import com.qa.framework.businessflows.PurchaseFlow;
import com.qa.framework.context.ContextKey;
import com.qa.framework.context.ScenarioContext;
import com.qa.framework.context.TestContext;
import com.qa.framework.data.CheckoutDataRow;
import com.qa.framework.data.RandomDataFactory;
import com.qa.framework.data.TestDataReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class CheckoutSteps {

    private static final double DELTA = 0.001;

    private final TestContext context;
    private final PurchaseFlow purchaseFlow;
    private final RandomDataFactory randomData;

    public CheckoutSteps(TestContext context, PurchaseFlow purchaseFlow, RandomDataFactory randomData) {
        this.context = context;
        this.purchaseFlow = purchaseFlow;
        this.randomData = randomData;
    }

    @When("the user purchases {string} with random customer details")
    public void theUserPurchasesWithRandomCustomerDetails(String product) {
        purchaseFlow.purchase(product, randomData.checkoutCustomer());
    }

    @Then("the order overview showed an item total of {double}")
    public void theOrderOverviewShowedAnItemTotalOf(double expected) {
        Assert.assertEquals(state().getDouble(ContextKey.ORDER_ITEM_TOTAL), expected, DELTA, "Unexpected item total");
    }

    @Then("the order total equalled the item total plus tax")
    public void theOrderTotalEqualledItemTotalPlusTax() {
        double expected = PurchaseFlow.expectedTotalWithTax(state().getDouble(ContextKey.ORDER_ITEM_TOTAL));
        Assert.assertEquals(state().getDouble(ContextKey.ORDER_TOTAL), expected, DELTA, "Order total is incorrect");
    }

    @Then("the order confirmation {string} is displayed")
    public void theOrderConfirmationIsDisplayed(String expected) {
        Assert.assertEquals(context.pages().checkoutCompletePage().confirmationHeader(), expected);
    }

    @Given("the user is on the checkout information step with {string} in the cart")
    public void theUserIsOnTheCheckoutInformationStep(String product) {
        purchaseFlow.openCheckoutWith(product);
    }

    @When("the user submits checkout details from data row {string}")
    public void theUserSubmitsCheckoutDetailsFromDataRow(String dataKey) {
        CheckoutDataRow row = TestDataReader.checkoutRow(dataKey);
        state().set(ContextKey.CHECKOUT_DATA_ROW, row);
        context.pages().checkoutInformationPage().submit(row.customer());
    }

    @Then("the expected checkout error for that data row is displayed")
    public void theExpectedCheckoutErrorIsDisplayed() {
        CheckoutDataRow row = state().get(ContextKey.CHECKOUT_DATA_ROW, CheckoutDataRow.class);
        Assert.assertEquals(context.pages().checkoutInformationPage().errorMessage(), row.expectedError(),
                "Wrong error for data row '" + row.dataKey() + "'");
    }

    @Then("the user stays on the checkout information step")
    public void theUserStaysOnTheCheckoutInformationStep() {
        Assert.assertTrue(context.pages().checkoutInformationPage().currentUrl().contains("checkout-step-one.html"),
                "User should remain on checkout step one, but URL was "
                        + context.pages().checkoutInformationPage().currentUrl());
    }

    private ScenarioContext state() {
        return context.scenarioContext();
    }
}
