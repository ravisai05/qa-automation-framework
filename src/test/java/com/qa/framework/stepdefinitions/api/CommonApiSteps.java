package com.qa.framework.stepdefinitions.api;

import com.qa.framework.context.ContextKey;
import com.qa.framework.context.TestContext;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.testng.Assert;

public class CommonApiSteps {

    private final TestContext context;

    public CommonApiSteps(TestContext context) {
        this.context = context;
    }

    @Then("the response status code is {int}")
    public void theResponseStatusCodeIs(int expected) {
        Response response = context.scenarioContext().get(ContextKey.LAST_RESPONSE, Response.class);
        Assert.assertEquals(response.statusCode(), expected, "Unexpected HTTP status. Body: " + response.asString());
    }
}
