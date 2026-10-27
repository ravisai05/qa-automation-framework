package com.qa.framework.api.constants;

public final class ApiHeaders {

    /** Restful Booker expects the auth token as a cookie: "Cookie: token=abc123". */
    public static final String COOKIE = "Cookie";
    public static final String TOKEN_COOKIE_PREFIX = "token=";

    private ApiHeaders() {
    }
}
