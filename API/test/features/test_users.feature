Feature: Users API

    Background:
        Given I have qa user inserted in the database

    Scenario: Login with invalid credentials
        When I login with invalid credentials
        Then I should get a 401 status code

    Scenario: Login with valid credentials
        When I login with valid credentials
        Then I should get a 200 status code

    Scenario: Accessing a protected route without a token
        When I access a protected route without a token
        Then I should get a 401 status code

    Scenario: Accessing a protected route with an invalid token
        When I access a protected route with an invalid token
        Then I should get a 401 status code

    Scenario: Accessing a protected route with a valid token
        When I access a protected route with a valid token
        Then I should get a 200 status code

    