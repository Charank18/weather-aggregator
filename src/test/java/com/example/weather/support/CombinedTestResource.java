// Quarkus test resource that composes WireMockTestResource and PostgresTestResource
// into a single lifecycle manager. Tests that need both a fake HTTP server and a real
// database can annotate with @QuarkusTestResource(CombinedTestResource.class) instead
// of managing two separate resources. start() merges the config maps from both; stop()
// shuts both down in order.
package com.example.weather.support;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Composite Quarkus test resource that starts both WireMock and a Testcontainers
 * PostgreSQL instance for tests that require the full application stack.
 *
 * <p>Usage: annotate your test class with
 * {@code @QuarkusTestResource(CombinedTestResource.class)}.</p>
 */
public class CombinedTestResource implements QuarkusTestResourceLifecycleManager {

    private final WireMockTestResource wireMock = new WireMockTestResource();
    private final PostgresTestResource postgres = new PostgresTestResource();

    /**
     * Starts both resources and merges their Quarkus config overrides into a
     * single map. WireMock config is added first; Postgres config is added second
     * (and wins on key conflicts, though none are expected).
     *
     * @return merged config map injected into the Quarkus test application
     */
    @Override
    public Map<String, String> start() {
        Map<String, String> config = new HashMap<>();
        config.putAll(wireMock.start());
        config.putAll(postgres.start());
        return config;
    }

    /** Stops both resources; order matches reverse-start (WireMock then Postgres). */
    @Override
    public void stop() {
        wireMock.stop();
        postgres.stop();
    }
}
