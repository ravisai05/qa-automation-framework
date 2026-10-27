package com.qa.framework.api.client;

import com.qa.framework.api.constants.Endpoints;
import com.qa.framework.api.request.AuthRequest;
import com.qa.framework.config.ConfigReader;
import io.restassured.response.Response;

public class AuthApiClient {

    static final String USERNAME_SECRET = "API_ADMIN_USERNAME";
    static final String PASSWORD_SECRET = "API_ADMIN_PASSWORD";

    private final ApiRequestFactory requests;

    public AuthApiClient(ApiRequestFactory requests) {
        this.requests = requests;
    }

    public Response createToken(AuthRequest credentials) {
        return requests.request().body(credentials).post(Endpoints.AUTH);
    }

    /** Uses the admin credentials from environment variables / CI secrets. */
    public Response createTokenWithConfiguredCredentials() {
        return createToken(new AuthRequest(ConfigReader.secret(USERNAME_SECRET), ConfigReader.secret(PASSWORD_SECRET)));
    }
}
