package com.qa.framework.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.qa.framework.api.request.BookingDates;
import com.qa.framework.api.request.BookingRequest;

/** Booking as returned by GET/PUT /booking/{id} and inside the POST /booking response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingDetails(
        String firstname,
        String lastname,
        int totalprice,
        boolean depositpaid,
        BookingDates bookingdates,
        String additionalneeds) {

    /** Same shape as the request, so tests can compare "what we sent" with "what we got" in one assertion. */
    public BookingRequest asRequest() {
        return new BookingRequest(firstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
    }
}
