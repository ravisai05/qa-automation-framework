package com.qa.framework.api.client;

import com.qa.framework.config.ConfigReader;
import com.qa.framework.context.TestContext;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Base request specification shared by all API clients of one scenario: base URI, JSON in/out, timeouts and
 * the logging filter. Built per scenario (PicoContainer) instead of mutating RestAssured's static defaults,
 * which would be shared - and raced on - by every parallel thread.
 */
public class ApiRequestFactory {

    private final RequestSpecification baseSpec;

    public ApiRequestFactory(TestContext context) {
        int timeoutMillis = ConfigReader.getInt("api.timeout.seconds") * 1000;
        RestAssuredConfig config = RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", timeoutMillis)
                .setParam("http.socket.timeout", timeoutMillis));
        this.baseSpec = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.get("api.base.url"))
                .setContentType(ContentType.JSON)
                // Send exactly "application/json". ContentType.JSON would send four media types
                // (application/json, application/javascript, text/javascript, text/json), and
                // Restful Booker answers POST /booking with 418 "I'm a Teapot" unless Accept is exactly JSON.
                .setAccept("application/json")
                .setConfig(config)
                .addFilter(new ApiLoggingFilter(context))
                .build();
    }

    public RequestSpecification request() {
        return RestAssured.given().spec(baseSpec);
    }
}
