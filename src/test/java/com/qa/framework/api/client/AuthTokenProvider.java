package com.qa.framework.api.client;

import com.qa.framework.api.response.AuthResponse;
import io.restassured.response.Response;

/**
 * Fetches the admin token on first use and reuses it for the rest of the scenario.
 * Scoped per scenario by PicoContainer: no token is shared between threads or leaks between scenarios.
 */
public class AuthTokenProvider {

    private final AuthApiClient authClient;
    private String token;

    public AuthTokenProvider(AuthApiClient authClient) {
        this.authClient = authClient;
    }

    public String token() {
        if (token == null) {
            Response response = authClient.createTokenWithConfiguredCredentials();
            AuthResponse body = response.statusCode() == 200 ? response.as(AuthResponse.class) : null;
            if (body == null || body.token() == null || body.token().isBlank()) {
                throw new IllegalStateException("Could not obtain an auth token: HTTP " + response.statusCode()
                        + (body == null ? "" : ", reason=" + body.reason())
                        + ". Check API_ADMIN_USERNAME / API_ADMIN_PASSWORD.");
            }
            token = body.token();
        }
        return token;
    }
}
