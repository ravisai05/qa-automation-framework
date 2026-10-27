package com.qa.framework.api.request;

/** Body of POST /booking and PUT /booking/{id}. Component names match the JSON field names. */
public record BookingRequest(
        String firstname,
        String lastname,
        int totalprice,
        boolean depositpaid,
        BookingDates bookingdates,
        String additionalneeds) {
}
