package com.qa.framework.businessflows;

import com.qa.framework.api.client.AuthTokenProvider;
import com.qa.framework.api.client.BookingApiClient;
import com.qa.framework.api.request.BookingRequest;
import com.qa.framework.api.request.BookingRequestBuilder;
import com.qa.framework.api.response.BookingCreatedResponse;
import com.qa.framework.api.response.BookingDetails;
import com.qa.framework.context.ContextKey;
import com.qa.framework.context.ScenarioContext;
import com.qa.framework.context.TestContext;
import io.restassured.response.Response;

/**
 * Booking lifecycle on top of the API clients. Every call stores the response (and, when successful, the
 * returned booking) in the ScenarioContext so the generic Then steps can assert on it.
 */
public class BookingFlow {

    private static final int OK = 200;

    private final ScenarioContext state;
    private final BookingApiClient bookings;
    private final AuthTokenProvider tokens;

    public BookingFlow(TestContext context, BookingApiClient bookings, AuthTokenProvider tokens) {
        this.state = context.scenarioContext();
        this.bookings = bookings;
        this.tokens = tokens;
    }

    public void usePayload(BookingRequest payload) {
        state.set(ContextKey.BOOKING_REQUEST, payload);
    }

    public void createBooking() {
        Response response = remember(bookings.create(payload()));
        if (response.statusCode() == OK) {
            BookingCreatedResponse created = response.as(BookingCreatedResponse.class);
            state.set(ContextKey.BOOKING_ID, created.bookingid());
            state.set(ContextKey.RETURNED_BOOKING, created.booking());
        }
    }

    /** Precondition for read/update/delete scenarios. */
    public void createExistingBooking(BookingRequest payload) {
        usePayload(payload);
        createBooking();
        FlowPreconditions.require(lastResponse().statusCode() == OK,
                "Could not create the booking: HTTP " + lastResponse().statusCode());
    }

    public void retrieveBooking() {
        retrieveBooking(bookingId());
    }

    public void retrieveBooking(int id) {
        rememberBooking(bookings.get(id));
    }

    public void updateBooking(String firstName, String additionalNeeds) {
        BookingRequest updated = changed(firstName, additionalNeeds);
        rememberBooking(bookings.update(bookingId(), updated, tokens.token()));
    }

    public void updateBookingWithoutToken() {
        remember(bookings.updateWithoutToken(bookingId(), changed("Unauthorised", "None")));
    }

    public void deleteBooking() {
        remember(bookings.delete(bookingId(), tokens.token()));
    }

    public void deleteBookingWithoutToken() {
        remember(bookings.deleteWithoutToken(bookingId()));
    }

    public BookingRequest payload() {
        return state.get(ContextKey.BOOKING_REQUEST, BookingRequest.class);
    }

    public BookingDetails returnedBooking() {
        return state.get(ContextKey.RETURNED_BOOKING, BookingDetails.class);
    }

    public boolean hasBookingId() {
        return bookingId() > 0;
    }

    public Response lastResponse() {
        return state.get(ContextKey.LAST_RESPONSE, Response.class);
    }

    private int bookingId() {
        return state.getInt(ContextKey.BOOKING_ID);
    }

    private BookingRequest changed(String firstName, String additionalNeeds) {
        return BookingRequestBuilder.from(payload()).firstname(firstName).additionalNeeds(additionalNeeds).build();
    }

    private void rememberBooking(Response response) {
        remember(response);
        if (response.statusCode() == OK) {
            state.set(ContextKey.RETURNED_BOOKING, response.as(BookingDetails.class));
        }
    }

    private Response remember(Response response) {
        state.set(ContextKey.LAST_RESPONSE, response);
        return response;
    }
}
