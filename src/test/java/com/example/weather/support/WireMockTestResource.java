// Quarkus test resource that starts a WireMock server on port 8089 before the test
// application boots and points both REST client config keys (open-meteo, geocoding-api)
// at it, so no real HTTP calls leave the test JVM.
// Provides three static stub helpers used by tests:
//   stubLondonSuccess()     — geocoding returns London's lat/lon; forecast returns 22.4°C / code 3
//   stubCityNotFound()      — geocoding returns an empty results array (triggers 404)
//   stubWeatherApiFailure() — geocoding succeeds but forecast returns HTTP 500
package com.example.weather.support;

import com.github.tomakehurst.wiremock.WireMockServer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Quarkus test resource that runs a WireMock server as a stand-in for the Open-Meteo
 * geocoding and forecast APIs. Both REST client base URLs are pointed at the WireMock
 * port so no real HTTP traffic leaves the test JVM.
 *
 * <p>The three static stub helpers allow individual tests to override the default
 * behaviour for specific failure scenarios.</p>
 */
public class WireMockTestResource implements QuarkusTestResourceLifecycleManager {

    /** Fixed port used for WireMock. Must not clash with the Quarkus test port (8081). */
    public static final int PORT = 8089;

    private static WireMockServer wireMockServer;

    /**
     * Starts WireMock, registers the default London-success stubs, and returns
     * Quarkus config overrides that redirect both REST clients to the mock server.
     *
     * @return map of Quarkus REST client URL config properties
     */
    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(PORT);
        wireMockServer.start();
        stubLondonSuccess(); // default happy-path stubs
        return Map.of(
                "quarkus.rest-client.open-meteo.url",    "http://localhost:" + PORT,
                "quarkus.rest-client.geocoding-api.url", "http://localhost:" + PORT
        );
    }

    /** Stops and nullifies the WireMock server after tests complete. */
    @Override
    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    // ── Stub helpers (called by individual tests to set up scenarios) ─────────

    /**
     * Registers the happy-path stubs:
     * <ul>
     *   <li>GET /v1/search → London at (51.5, -0.1)</li>
     *   <li>GET /v1/forecast → 22.4°C, 14.2 km/h, WMO code 3 ("Overcast")</li>
     * </ul>
     * Resets all previously registered stubs first so tests start from a clean slate.
     */
    public static void stubLondonSuccess() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            return;
        }

        wireMockServer.resetAll();

        // Geocoding stub — returns London coordinates
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/search"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                          "results": [
                            {
                              "latitude": 51.5,
                              "longitude": -0.1,
                              "name": "London"
                            }
                          ]
                        }
                        """)));

        // Forecast stub — returns current weather with WMO code 3 (Overcast)
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/forecast"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                          "current_weather": {
                            "temperature": 22.4,
                            "windspeed": 14.2,
                            "weathercode": 3
                          }
                        }
                        """)));
    }

    /**
     * Registers a stub that simulates an unknown city:
     * GET /v1/search returns an empty {@code results} array.
     * The service will throw {@code CityNotFoundException}, resulting in HTTP 404.
     */
    public static void stubCityNotFound() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            return;
        }

        wireMockServer.resetAll();

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/search"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                          "results": []
                        }
                        """)));
    }

    /**
     * Registers stubs that simulate a forecast API failure:
     * geocoding succeeds (London found), but GET /v1/forecast returns HTTP 500.
     * The service will propagate the error, resulting in HTTP 500.
     */
    public static void stubWeatherApiFailure() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            return;
        }

        wireMockServer.resetAll();

        // Geocoding still succeeds so we reach the forecast call
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/search"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                          "results": [
                            {
                              "latitude": 51.5,
                              "longitude": -0.1,
                              "name": "London"
                            }
                          ]
                        }
                        """)));

        // Forecast returns a server error
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/forecast"))
                .willReturn(aResponse().withStatus(500)));
    }
}
