@api @booking
Feature: Booking API
  As an API consumer
  I want to create, read, update and delete bookings
  So that the booking lifecycle works end to end

  @TC-API-003 @smoke @regression
  Scenario: Create a booking from test data
    Given a booking payload "standard_booking" from test data
    When the client creates the booking
    Then the response status code is 200
    And the response contains a booking id
    And the returned booking matches the payload

  @TC-API-004 @regression
  Scenario: Retrieve a booking by id
    Given an existing booking created from a random payload
    When the client retrieves the booking
    Then the response status code is 200
    And the returned booking matches the payload

  @TC-API-005 @regression
  Scenario: Update a booking with a valid token
    Given an existing booking created from "no_deposit_booking"
    When the client updates the booking with first name "Jordan" and additional needs "Airport pickup"
    Then the response status code is 200
    And the returned booking has first name "Jordan" and additional needs "Airport pickup"

  @TC-API-006 @regression
  Scenario: Delete a booking with a valid token
    Given an existing booking created from "standard_booking"
    When the client deletes the booking
    Then the response status code is 201
    And the booking can no longer be retrieved

  @TC-API-007 @regression @negative
  Scenario: Update without a token is forbidden
    Given an existing booking created from "standard_booking"
    When the client updates the booking without an auth token
    Then the response status code is 403

  @TC-API-008 @regression @negative
  Scenario: Delete without a token is forbidden
    Given an existing booking created from "standard_booking"
    When the client deletes the booking without an auth token
    Then the response status code is 403

  @TC-API-009 @regression @negative
  Scenario: Unknown booking id returns not found
    When the client retrieves booking id 999999999
    Then the response status code is 404
