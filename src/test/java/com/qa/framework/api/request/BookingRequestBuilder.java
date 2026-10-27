package com.qa.framework.api.request;

/** Fluent builder so tests only spell out the fields they care about. */
public final class BookingRequestBuilder {

    private String firstname;
    private String lastname;
    private int totalPrice;
    private boolean depositPaid;
    private BookingDates dates;
    private String additionalNeeds;

    private BookingRequestBuilder() {
    }

    public static BookingRequestBuilder aBooking() {
        return new BookingRequestBuilder();
    }

    /** Starts from an existing payload, e.g. to change two fields for an update. */
    public static BookingRequestBuilder from(BookingRequest source) {
        return aBooking()
                .firstname(source.firstname())
                .lastname(source.lastname())
                .totalPrice(source.totalprice())
                .depositPaid(source.depositpaid())
                .dates(source.bookingdates().checkin(), source.bookingdates().checkout())
                .additionalNeeds(source.additionalneeds());
    }

    public BookingRequestBuilder firstname(String value) {
        this.firstname = value;
        return this;
    }

    public BookingRequestBuilder lastname(String value) {
        this.lastname = value;
        return this;
    }

    public BookingRequestBuilder totalPrice(int value) {
        this.totalPrice = value;
        return this;
    }

    public BookingRequestBuilder depositPaid(boolean value) {
        this.depositPaid = value;
        return this;
    }

    public BookingRequestBuilder dates(String checkin, String checkout) {
        this.dates = new BookingDates(checkin, checkout);
        return this;
    }

    public BookingRequestBuilder additionalNeeds(String value) {
        this.additionalNeeds = value;
        return this;
    }

    public BookingRequest build() {
        if (firstname == null || lastname == null || dates == null) {
            throw new IllegalStateException("firstname, lastname and dates are mandatory for a booking");
        }
        return new BookingRequest(firstname, lastname, totalPrice, depositPaid, dates, additionalNeeds);
    }
}
