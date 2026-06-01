// Pact consumer contract tests for the WeatherAggregator → OpenMeteo interactions.
// Defines two pacts (saved to target/pacts/):
//   geocodingPact  — GET /v1/search returns a results array with London's lat/lon
//   forecastPact   — GET /v1/forecast returns a current_weather block with temp/wind/code
// Each @Test method fires a raw HTTP request at the Pact mock server and asserts the
// response status and that key JSON fields are present in the body.
package com.example.weather.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Consumer-side Pact contract tests for the WeatherAggregator → OpenMeteo interaction.
 *
 * <p>Each {@code @Pact} method defines an expected interaction and writes it to
 * {@code target/pacts/WeatherAggregator-OpenMeteo.json}. The corresponding {@code @Test}
 * verifies that a real HTTP request to the Pact mock server satisfies the contract.</p>
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "OpenMeteo")
class OpenMeteoPactConsumerTest {

    /**
     * Contract: when the city "London" exists, GET /v1/search returns a results
     * array containing the city's latitude, longitude, and name.
     */
    @Pact(provider = "OpenMeteo", consumer = "WeatherAggregator")
    public V4Pact geocodingPact(PactDslWithProvider builder) {
        return builder
                .given("City London exists")
                .uponReceiving("Geocoding search for London")
                .path("/v1/search")
                .query("name=London&count=1&language=en&format=json")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("""
                    {
                      "results": [
                        {
                          "latitude": 51.5,
                          "longitude": -0.1,
                          "name": "London",
                          "country": "United Kingdom"
                        }
                      ]
                    }
                    """)
                .toPact(V4Pact.class);
    }

    /**
     * Contract: GET /v1/forecast for London's coordinates returns a
     * {@code current_weather} block with temperature, wind speed, and weather code.
     */
    @Pact(provider = "OpenMeteo", consumer = "WeatherAggregator")
    public V4Pact forecastPact(PactDslWithProvider builder) {
        return builder
                .given("Current weather for coordinates")
                .uponReceiving("Forecast for coordinates")
                .path("/v1/forecast")
                .query("latitude=51.5&longitude=-0.1&current_weather=true&wind_speed_unit=kmh")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("""
                    {
                      "current_weather": {
                        "temperature": 22.4,
                        "windspeed": 14.2,
                        "weathercode": 3,
                        "time": "2024-06-01T14:00"
                      }
                    }
                    """)
                .toPact(V4Pact.class);
    }

    /**
     * Fires a real GET request at the Pact mock server for the geocoding pact
     * and verifies the response is 200 and contains London's coordinates.
     */
    @Test
    @PactTestFor(pactMethod = "geocodingPact")
    void geocodingContract(MockServer mockServer) throws IOException {
        URI uri = URI.create(mockServer.getUrl() + "/v1/search?name=London&count=1&language=en&format=json");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());

        String response;
        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            response = scanner.useDelimiter("\\A").next();
        }

        // Verify the key fields the application depends on are present
        assertTrue(response.contains("London"));
        assertTrue(response.contains("latitude"));
    }

    /**
     * Fires a real GET request at the Pact mock server for the forecast pact
     * and verifies the response is 200 and contains the current_weather block.
     */
    @Test
    @PactTestFor(pactMethod = "forecastPact")
    void forecastContract(MockServer mockServer) throws IOException {
        URI uri = URI.create(
                mockServer.getUrl()
                        + "/v1/forecast?latitude=51.5&longitude=-0.1&current_weather=true&wind_speed_unit=kmh"
        );
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());

        String response;
        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            response = scanner.useDelimiter("\\A").next();
        }

        // Verify the key fields the application depends on are present
        assertTrue(response.contains("current_weather"));
        assertTrue(response.contains("temperature"));
    }
}
