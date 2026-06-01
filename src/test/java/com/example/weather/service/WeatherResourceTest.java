// Integration tests for WeatherResource (the REST layer) running in a full Quarkus
// application with WireMock (fake Open-Meteo) and Postgres (Testcontainers) via CombinedTestResource.
// Covers three scenarios:
//   - successful fetch returns 200 with correct city and weatherDescription
//   - unknown city (geocoding returns empty results) → 404 with error message
//   - forecast API returns 500 → propagated as HTTP 500 to the caller
package com.example.weather.service;

import com.example.weather.support.CombinedTestResource;
import com.example.weather.support.WireMockTestResource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Full-stack integration tests for the REST layer ({@code WeatherResource}).
 * The Quarkus application runs with WireMock standing in for Open-Meteo and
 * a Testcontainers PostgreSQL instance for persistence.
 */
@QuarkusTest
@QuarkusTestResource(CombinedTestResource.class)
class WeatherResourceTest {

    /** Reset WireMock stubs to the default "London success" state before each test. */
    @BeforeEach
    void resetStubs() {
        WireMockTestResource.stubLondonSuccess();
    }

    /**
     * POST /weather/fetch?city=London returns 200 with the correct city
     * and a human-readable weather description mapped from WMO code 3 ("Overcast").
     */
    @Test
    void shouldFetchAndStoreWeather() {

        given()
                .contentType("application/json")
                .when()
                .post("/weather/fetch?city=London")
                .then()
                .statusCode(200)
                .body("city", equalTo("London"))
                .body("weatherDescription", equalTo("Overcast"));
    }

    /**
     * When the geocoding stub returns an empty results array, the service throws
     * {@code CityNotFoundException} which the exception mapper converts to 404.
     */
    @Test
    void shouldReturn404WhenCityNotFound() {

        WireMockTestResource.stubCityNotFound();

        given()
                .when()
                .post("/weather/fetch?city=xyznotacity123")
                .then()
                .statusCode(404)
                .body("error", equalTo("City not found: xyznotacity123"));
    }

    /**
     * When the forecast stub returns HTTP 500, the error must propagate through
     * the stack and be returned to the caller as HTTP 500.
     */
    @Test
    void shouldReturn500WhenWeatherApiFails() {

        WireMockTestResource.stubWeatherApiFailure();

        given()
                .when()
                .post("/weather/fetch?city=London")
                .then()
                .statusCode(500);
    }
}
