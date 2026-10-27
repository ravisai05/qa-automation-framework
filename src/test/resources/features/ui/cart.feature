@ui @cart
Feature: Shopping cart
  As a logged-in shopper
  I want to add and remove products from my cart
  So that I only buy what I need

  Background:
    Given the user is logged in as the "standard" user

  @TC-UI-005 @smoke @regression
  Scenario: Add a single product to the cart
    When the user adds "Sauce Labs Backpack" to the cart
    Then the cart badge shows 1
    And the cart contains "Sauce Labs Backpack"

  @TC-UI-006 @regression
  Scenario: Add several products to the cart
    When the user adds the following products to the cart:
      | Sauce Labs Backpack     |
      | Sauce Labs Bike Light   |
      | Sauce Labs Bolt T-Shirt |
    Then the cart badge shows 3

  @TC-UI-007 @regression
  Scenario: Remove a product from the products page
    Given the user adds "Sauce Labs Backpack" to the cart
    When the user removes "Sauce Labs Backpack" from the products page
    Then the cart badge is not displayed

  @TC-UI-008 @regression
  Scenario: Remove a product from the cart page
    Given the user adds "Sauce Labs Backpack" to the cart
    And the user adds "Sauce Labs Onesie" to the cart
    When the user opens the cart
    And the user removes "Sauce Labs Onesie" from the cart page
    Then the cart contains only "Sauce Labs Backpack"
    And the cart badge shows 1
