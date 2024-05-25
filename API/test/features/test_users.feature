Feature: Users API
    
    Scenario: User cannot register with an existing email
        Given I have qa user inserted in the database
        When I register with an existing email
        Then I should get a 409 status code
    
    Scenario: User cannot register with an existing username
        Given I have qa user inserted in the database
        When I register with an existing username
        Then I should get a 409 status code

    Scenario: User can register with a new email and username
        When I register with a new email and username
        Then I should get a 201 status code

    Scenario: Login with invalid credentials
        Given I have qa user inserted in the database
        When I login with invalid credentials
        Then I should get a 401 status code

    Scenario: Login with valid credentials
        Given I have qa user inserted in the database
        When I login with valid credentials
        Then I should get a 200 status code

    Scenario: Accessing a protected route without a token
        Given I have qa user inserted in the database
        When I access a protected route without a token
        Then I should get a 401 status code

    Scenario: Accessing a protected route with an invalid token
        Given I have qa user inserted in the database
        When I access a protected route with an invalid token
        Then I should get a 401 status code

    Scenario: Accessing a protected route with a valid token
        Given I have qa user inserted in the database
        When I access a protected route with a valid token
        Then I should get a 200 status code

