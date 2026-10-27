@api @auth
Feature: Authentication API
  As an API consumer
  I want to exchange admin credentials for a token
  So that I can call protected booking endpoints

  @TC-API-001 @smoke @regression
  Scenario: Create a token with valid admin credentials
    When the client requests a token with the configured admin credentials
    Then the response status code is 200
    And a non-empty auth token is returned

  @TC-API-002 @regression
  Scenario: Token request with wrong credentials is refused
    When the client requests a token with username "admin" and password "not-the-password"
    Then the response status code is 200
    And the auth response reason is "Bad credentials"
