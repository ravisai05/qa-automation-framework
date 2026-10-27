package com.qa.framework.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** POST /booking -> {"bookingid": 1, "booking": {...}} */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingCreatedResponse(int bookingid, BookingDetails booking) {
}
