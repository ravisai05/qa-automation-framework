@ui @login
Feature: Login
  As a shopper on the demo store
  I want to sign in with my account
  So that I can browse and buy products

  Background:
    Given the user is on the login page

  @TC-UI-001 @smoke @regression
  Scenario: Standard user logs in successfully
    When the user logs in as the "standard" user
    Then the products page is displayed

  @TC-UI-002 @regression
  Scenario: Locked-out user is refused
    When the user logs in as the "locked" user
    Then the login error message "Epic sadface: Sorry, this user has been locked out." is displayed

  @TC-UI-003 @regression
  Scenario Outline: Login is rejected for invalid input
    When the user logs in with username "<username>" and password "<password>"
    Then the login error message "<error>" is displayed

    Examples:
      | username      | password       | error                                                                     |
      | standard_user | wrong_password | Epic sadface: Username and password do not match any user in this service |
      |               | any_password   | Epic sadface: Username is required                                        |
      | standard_user |                | Epic sadface: Password is required                                        |

  @TC-UI-004 @regression
  Scenario: Problem user can still reach the products page
    When the user logs in as the "problem" user
    Then the products page is displayed
