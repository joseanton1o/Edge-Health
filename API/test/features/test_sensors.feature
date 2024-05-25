Feature: sensors API

Scenario: Provisioning a new sensor data
    Given I have a qa user authenticated
    When I send new valid sensor data
    Then the response status code should be 201

Scenario: Retreiving sensor data with valid user
    Given I have a qa user authenticated
    When I send new valid sensor data 
    And I send a request to retrieve sensor data 
    Then the response status code should be 200
    And the response should contain the number of data I sent

Scenario: Provisioning invalid sensor data
    Given I have a qa user authenticated
    When I send new invalid sensor data
    Then the response status code should be 400

Scenario: Provisioning sensor data with invalid user
    When I send new valid sensor data with false auth token
    Then the response status code should be 401

Scenario: Provisioning sensor data with no auth token
    When I send a request to provision sensor data without authentication
    Then the response status code should be 401