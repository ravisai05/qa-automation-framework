package com.qa.framework.stepdefinitions.api;

import com.qa.framework.api.client.AuthApiClient;
import com.qa.framework.api.request.AuthRequest;
import com.qa.framework.api.response.AuthResponse;
import com.qa.framework.context.ContextKey;
import com.qa.framework.context.ScenarioContext;
import com.qa.framework.context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;

public class AuthSteps {

    private final ScenarioContext state;
    private final AuthApiClient authClient;

    public AuthSteps(TestContext context, AuthApiClient authClient) {
        this.state = context.scenarioContext();
        this.authClient = authClient;
    }

    @When("the client requests a token with the configured admin credentials")
    public void requestTokenWithConfiguredCredentials() {
        state.set(ContextKey.LAST_RESPONSE, authClient.createTokenWithConfiguredCredentials());
    }

    @When("the client requests a token with username {string} and password {string}")
    public void requestTokenWith(String username, String password) {
        state.set(ContextKey.LAST_RESPONSE, authClient.createToken(new AuthRequest(username, password)));
    }

    @Then("a non-empty auth token is returned")
    public void aNonEmptyAuthTokenIsReturned() {
        String token = authResponse().token();
        Assert.assertTrue(token != null && !token.isBlank(), "Expected a token but got none");
    }

    @Then("the auth response reason is {string}")
    public void theAuthResponseReasonIs(String expected) {
        AuthResponse body = authResponse();
        Assert.assertEquals(body.reason(), expected);
        Assert.assertNull(body.token(), "No token must be issued for bad credentials");
    }

    private AuthResponse authResponse() {
        return state.get(ContextKey.LAST_RESPONSE, Response.class).as(AuthResponse.class);
    }
}
