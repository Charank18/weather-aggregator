// Pact provider verification test for the "OpenMeteo" provider role.
// Starts a WireMock server on a random port as a stand-in for the real Open-Meteo API,
// loads pacts from src/test/resources/pacts/, and replays each interaction against the
// WireMock stubs to verify the provider side of the contract.
// Provider states ("City London exists", "Current weather for coordinates") configure
// the appropriate WireMock stub before each interaction is verified.
package com.example.weather.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;

import com.github.tomakehurst.wiremock.WireMockServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Provider-side Pact verification for the "OpenMeteo" provider.
 *
 * <p>A WireMock server stands in for the real Open-Meteo API. Pact loads
 * the consumer contracts from {@code src/test/resources/pacts/} and replays
 * each recorded interaction against the WireMock stubs to confirm the provider
 * honours its side of the contract.</p>
 */
@Provider("OpenMeteo")
@PactFolder("src/test/resources/pacts")
class OpenMeteoPactProviderTest {

    /** WireMock server that impersonates Open-Meteo; started on a random free port. */
    static WireMockServer wireMockServer;

    /** Start WireMock once before any test in this class runs. */
    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0); // port 0 = pick a random free port
        wireMockServer.start();
    }

    /** Stop WireMock after all tests have finished. */
    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    /** Point the Pact verification context at the WireMock server before each interaction. */
    @BeforeEach
    void setTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", wireMockServer.port()));
    }

    /** Pact replays each recorded interaction and delegates verification here. */
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPactAgainstProvider(PactVerificationContext context) {
        context.verifyInteraction();
    }

    // ── Provider state setup ──────────────────────────────────────────────────

    /**
     * Configures WireMock to return a valid London geocoding result,
     * satisfying the "City London exists" provider state.
     */
    @State("City London exists")
    void cityLondonExists() {
        wireMockServer.resetAll();
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/search"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
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
                        """)));
    }

    /**
     * Configures WireMock to return a valid forecast block,
     * satisfying the "Current weather for coordinates" provider state.
     */
    @State("Current weather for coordinates")
    void currentWeatherForCoordinates() {
        wireMockServer.resetAll();
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/forecast"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                          "current_weather": {
                            "temperature": 22.4,
                            "windspeed": 14.2,
                            "weathercode": 3,
                            "time": "2024-06-01T14:00"
                          }
                        }
                        """)));
    }
}
