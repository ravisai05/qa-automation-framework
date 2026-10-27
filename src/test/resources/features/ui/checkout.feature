@ui @checkout
Feature: Checkout
  As a shopper with items in my cart
  I want to complete the checkout
  So that my order is placed

  Background:
    Given the user is logged in as the "standard" user

  @TC-UI-012 @smoke @regression
  Scenario: Complete an order end to end with generated customer details
    When the user purchases "Sauce Labs Backpack" with random customer details
    Then the order overview showed an item total of 29.99
    And the order total equalled the item total plus tax
    And the order confirmation "Thank you for your order!" is displayed

  @TC-UI-013 @regression
  Scenario Outline: Checkout form rejects missing customer details (data from Excel)
    Given the user is on the checkout information step with "Sauce Labs Backpack" in the cart
    When the user submits checkout details from data row "<dataKey>"
    Then the expected checkout error for that data row is displayed
    And the user stays on the checkout information step

    Examples:
      | dataKey             |
      | missing_first_name  |
      | missing_last_name   |
      | missing_postal_code |
