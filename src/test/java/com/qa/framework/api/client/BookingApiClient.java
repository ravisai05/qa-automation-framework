package com.qa.framework.api.client;

import com.qa.framework.api.constants.ApiHeaders;
import com.qa.framework.api.constants.Endpoints;
import com.qa.framework.api.request.BookingRequest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** One method per endpoint; returns the raw Response so tests can assert status codes, including errors. */
public class BookingApiClient {

    private final ApiRequestFactory requests;

    public BookingApiClient(ApiRequestFactory requests) {
        this.requests = requests;
    }

    public Response create(BookingRequest booking) {
        return requests.request().body(booking).post(Endpoints.BOOKING);
    }

    public Response get(int bookingId) {
        return requests.request().pathParam(Endpoints.ID_PARAM, bookingId).get(Endpoints.BOOKING_BY_ID);
    }

    public Response update(int bookingId, BookingRequest booking, String token) {
        return withToken(token).pathParam(Endpoints.ID_PARAM, bookingId).body(booking).put(Endpoints.BOOKING_BY_ID);
    }

    /** Deliberately unauthenticated - for negative tests. */
    public Response updateWithoutToken(int bookingId, BookingRequest booking) {
        return requests.request().pathParam(Endpoints.ID_PARAM, bookingId).body(booking).put(Endpoints.BOOKING_BY_ID);
    }

    public Response delete(int bookingId, String token) {
        return withToken(token).pathParam(Endpoints.ID_PARAM, bookingId).delete(Endpoints.BOOKING_BY_ID);
    }

    /** Deliberately unauthenticated - for negative tests. */
    public Response deleteWithoutToken(int bookingId) {
        return requests.request().pathParam(Endpoints.ID_PARAM, bookingId).delete(Endpoints.BOOKING_BY_ID);
    }

    private RequestSpecification withToken(String token) {
        return requests.request().header(ApiHeaders.COOKIE, ApiHeaders.TOKEN_COOKIE_PREFIX + token);
    }
}
