Feature: Weather API

  Scenario: Fetch weather for a city

    When I fetch weather for "London"

    Then the response city should be "London"


  Scenario: Retrieve latest weather

    When I fetch weather for "London"

    And I request latest weather for "London"

    Then latest response city should be "London"


  Scenario: Retrieve all readings

    When I fetch weather for "London"

    And I request all weather readings for "London"

    Then the readings list should not be empty