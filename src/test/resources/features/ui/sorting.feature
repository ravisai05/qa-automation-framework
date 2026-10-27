@ui @sorting
Feature: Product sorting
  As a shopper
  I want to sort the product list
  So that I can find products by price or name quickly

  Background:
    Given the user is logged in as the "standard" user

  @TC-UI-009 @regression
  Scenario: Sort products by price from low to high
    When the user sorts products by "Price (low to high)"
    Then the product prices are sorted in ascending order

  @TC-UI-010 @regression
  Scenario: Sort products by price from high to low
    When the user sorts products by "Price (high to low)"
    Then the product prices are sorted in descending order

  @TC-UI-011 @regression
  Scenario: Sort products by name from Z to A
    When the user sorts products by "Name (Z to A)"
    Then the product names are sorted in reverse alphabetical order
