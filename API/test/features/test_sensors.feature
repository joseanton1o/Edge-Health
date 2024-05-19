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