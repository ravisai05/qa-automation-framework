package com.qa.framework.stepdefinitions.api;

import com.qa.framework.api.response.BookingDetails;
import com.qa.framework.businessflows.BookingFlow;
import com.qa.framework.data.RandomDataFactory;
import com.qa.framework.data.TestDataReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class BookingSteps {

    private static final int NOT_FOUND = 404;

    private final BookingFlow bookingFlow;
    private final RandomDataFactory randomData;

    public BookingSteps(BookingFlow bookingFlow, RandomDataFactory randomData) {
        this.bookingFlow = bookingFlow;
        this.randomData = randomData;
    }

    @Given("a booking payload {string} from test data")
    public void aBookingPayloadFromTestData(String key) {
        bookingFlow.usePayload(TestDataReader.booking(key));
    }

    @Given("an existing booking created from {string}")
    public void anExistingBookingCreatedFrom(String key) {
        bookingFlow.createExistingBooking(TestDataReader.booking(key));
    }

    @Given("an existing booking created from a random payload")
    public void anExistingBookingCreatedFromARandomPayload() {
        bookingFlow.createExistingBooking(randomData.booking());
    }

    @When("the client creates the booking")
    public void theClientCreatesTheBooking() {
        bookingFlow.createBooking();
    }

    @When("the client retrieves the booking")
    public void theClientRetrievesTheBooking() {
        bookingFlow.retrieveBooking();
    }

    @When("the client retrieves booking id {int}")
    public void theClientRetrievesBookingId(int id) {
        bookingFlow.retrieveBooking(id);
    }

    @When("the client updates the booking with first name {string} and additional needs {string}")
    public void theClientUpdatesTheBooking(String firstName, String additionalNeeds) {
        bookingFlow.updateBooking(firstName, additionalNeeds);
    }

    @When("the client updates the booking without an auth token")
    public void theClientUpdatesTheBookingWithoutAToken() {
        bookingFlow.updateBookingWithoutToken();
    }

    @When("the client deletes the booking")
    public void theClientDeletesTheBooking() {
        bookingFlow.deleteBooking();
    }

    @When("the client deletes the booking without an auth token")
    public void theClientDeletesTheBookingWithoutAToken() {
        bookingFlow.deleteBookingWithoutToken();
    }

    @Then("the response contains a booking id")
    public void theResponseContainsABookingId() {
        Assert.assertTrue(bookingFlow.hasBookingId(), "No positive booking id in the create response");
    }

    @Then("the returned booking matches the payload")
    public void theReturnedBookingMatchesThePayload() {
        Assert.assertEquals(bookingFlow.returnedBooking().asRequest(), bookingFlow.payload());
    }

    @Then("the returned booking has first name {string} and additional needs {string}")
    public void theReturnedBookingHas(String firstName, String additionalNeeds) {
        BookingDetails booking = bookingFlow.returnedBooking();
        Assert.assertEquals(booking.firstname(), firstName);
        Assert.assertEquals(booking.additionalneeds(), additionalNeeds);
    }

    @Then("the booking can no longer be retrieved")
    public void theBookingCanNoLongerBeRetrieved() {
        bookingFlow.retrieveBooking();
        Assert.assertEquals(bookingFlow.lastResponse().statusCode(), NOT_FOUND, "Deleted booking is still retrievable");
    }
}
