package com.qa.framework.stepdefinitions.ui;

import com.qa.framework.businessflows.LoginFlow;
import com.qa.framework.context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class LoginSteps {

    private final TestContext context;
    private final LoginFlow loginFlow;

    public LoginSteps(TestContext context, LoginFlow loginFlow) {
        this.context = context;
        this.loginFlow = loginFlow;
    }

    @Given("the user is on the login page")
    public void theUserIsOnTheLoginPage() {
        loginFlow.openLoginPage();
    }

    @Given("the user is logged in as the {string} user")
    public void theUserIsLoggedInAs(String alias) {
        loginFlow.loginAndOpenProducts(alias);
    }

    @When("the user logs in as the {string} user")
    public void theUserLogsInAs(String alias) {
        loginFlow.loginAs(alias);
    }

    @When("the user logs in with username {string} and password {string}")
    public void theUserLogsInWith(String username, String password) {
        loginFlow.loginWith(username, password);
    }

    @Then("the products page is displayed")
    public void theProductsPageIsDisplayed() {
        Assert.assertTrue(context.pages().inventoryPage().isLoaded(), "Products page was not displayed");
        Assert.assertTrue(context.pages().inventoryPage().productCount() > 0, "No products were listed");
    }

    @Then("the login error message {string} is displayed")
    public void theLoginErrorMessageIsDisplayed(String expected) {
        Assert.assertEquals(context.pages().loginPage().errorMessage(), expected);
        Assert.assertTrue(context.pages().loginPage().isLoginButtonDisplayed(), "User should remain on the login page");
    }
}
