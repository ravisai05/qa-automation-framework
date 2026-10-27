package com.qa.framework.api.request;

/** ISO dates (yyyy-MM-dd) as the API expects them. */
public record BookingDates(String checkin, String checkout) {
}
