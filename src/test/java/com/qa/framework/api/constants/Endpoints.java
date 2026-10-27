package com.qa.framework.api.constants;

/** Restful Booker endpoint paths (base URL comes from config: api.base.url). */
public final class Endpoints {

    public static final String AUTH = "/auth";
    public static final String BOOKING = "/booking";
    public static final String BOOKING_BY_ID = "/booking/{id}";
    public static final String ID_PARAM = "id";

    private Endpoints() {
    }
}
