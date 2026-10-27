package com.qa.framework.context;

/** Typed keys for values shared between step classes within ONE scenario. */
public enum ContextKey {
    /** Last REST Assured response (io.restassured.response.Response). */
    LAST_RESPONSE,
    /** Booking payload that was sent (BookingRequest). */
    BOOKING_REQUEST,
    /** Booking as returned by the API in the last successful call (BookingDetails). */
    RETURNED_BOOKING,
    /** Id of the booking created in this scenario (Integer). */
    BOOKING_ID,
    /** Excel row used for the checkout form (CheckoutDataRow). */
    CHECKOUT_DATA_ROW,
    /** Item total read from the checkout overview (Double). */
    ORDER_ITEM_TOTAL,
    /** Grand total read from the checkout overview (Double). */
    ORDER_TOTAL
}
