// Cucumber step definitions that drive the live Quarkus HTTP server (via RestAssured)
// to implement the weather.feature scenarios.
// Before each scenario the WireMock stubs are reset to the "London success" state.
// Steps cover: fetching weather for a city, reading the latest reading, and
// listing all readings — verifying city name and non-empty list in the responses.
package com.example.weather.bdd;

import com.example.weather.support.WireMockTestResource;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber glue code for {@code features/weather.feature}.
 * Each method maps to a Gherkin step and drives the running Quarkus server via RestAssured.
 */
public class WeatherStepDefinitions {

    /** Holds the last HTTP response so multiple Then/And steps can inspect it. */
    Response response;

    /** Re-register the default London success stubs before every scenario. */
    @Before
    public void resetStubs() {
        WireMockTestResource.stubLondonSuccess();
    }

    // ── When steps ────────────────────────────────────────────────────────────

    /** POST /weather/fetch?city={city} — triggers a live fetch and store. */
    @When("I fetch weather for {string}")
    public void fetchWeather(String city) {
        response =
                given()
                        .contentType("application/json")
                        .when()
                        .post("/weather/fetch?city=" + city);
    }

    /** GET /weather/{city}/latest — retrieves the most recent stored reading. */
    @And("I request latest weather for {string}")
    public void latestWeather(String city) {
        response =
                given()
                        .when()
                        .get("/weather/" + city + "/latest");
    }

    /** GET /weather/{city} — retrieves all stored readings for the city. */
    @And("I request all weather readings for {string}")
    public void allReadings(String city) {
        response =
                given()
                        .when()
                        .get("/weather/" + city);
    }

    // ── Then steps ────────────────────────────────────────────────────────────

    /** Asserts the {@code city} field in the last fetch response matches the expected value. */
    @Then("the response city should be {string}")
    public void verifyCity(String city) {
        assertEquals(city, response.jsonPath().getString("city"));
    }

    /** Asserts the {@code city} field in the latest-reading response matches the expected value. */
    @Then("latest response city should be {string}")
    public void verifyLatestCity(String city) {
        assertEquals(city, response.jsonPath().getString("city"));
    }

    /** Asserts the all-readings response contains at least one entry. */
    @Then("the readings list should not be empty")
    public void verifyReadings() {
        assertFalse(response.jsonPath().getList("$").isEmpty());
    }
}
