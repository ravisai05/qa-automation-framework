package com.qa.framework.data;

import com.github.javafaker.Faker;
import com.qa.framework.api.request.BookingRequest;
import com.qa.framework.api.request.BookingRequestBuilder;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Random but realistic data (JavaFaker). Injected per scenario by PicoContainer, so each scenario has its own
 * Faker instance. Generated values are attached to the report through the page/API logging, so a failing run
 * can be reproduced.
 */
public class RandomDataFactory {

    private final Faker faker = new Faker(Locale.US);

    public CheckoutCustomer checkoutCustomer() {
        return new CheckoutCustomer(faker.name().firstName(), faker.name().lastName(), faker.address().zipCode());
    }

    public BookingRequest booking() {
        LocalDate checkin = LocalDate.now().plusDays(faker.number().numberBetween(10, 60));
        LocalDate checkout = checkin.plusDays(faker.number().numberBetween(1, 7));
        return BookingRequestBuilder.aBooking()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalPrice(faker.number().numberBetween(80, 900))
                .depositPaid(faker.bool().bool())
                .dates(checkin.toString(), checkout.toString())
                .additionalNeeds(faker.options().option("Breakfast", "Late checkout", "Extra pillow", "Parking"))
                .build();
    }
}
