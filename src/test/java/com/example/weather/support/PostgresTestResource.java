// Quarkus test resource that starts a real PostgreSQL 16 container via Testcontainers
// before the test application boots. Injects the container's JDBC URL, username, and
// password into the Quarkus datasource config so tests hit an isolated throwaway database
// rather than the production one. The container is stopped when the test suite finishes.
package com.example.weather.support;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

/**
 * Quarkus test resource that provisions a throwaway PostgreSQL 16 container
 * via Testcontainers and wires its connection details into the Quarkus datasource config.
 *
 * <p>The same static container instance is reused across all tests in a JVM run
 * (Testcontainers reuse pattern) to avoid the overhead of starting a new container per test.</p>
 */
public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

    /**
     * Static container shared across all tests.
     * Configured with the same database name, username, and password as production
     * so no application code needs environment-specific branches.
     */
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("weatherdb")
                    .withUsername("weather")
                    .withPassword("weather");

    /**
     * Starts the container and returns Quarkus config overrides that point the
     * datasource at the container's dynamically assigned port.
     *
     * @return map of Quarkus datasource config properties
     */
    @Override
    public Map<String, String> start() {
        POSTGRES.start();
        return Map.of(
                "quarkus.datasource.jdbc.url", POSTGRES.getJdbcUrl(),
                "quarkus.datasource.username",  POSTGRES.getUsername(),
                "quarkus.datasource.password",  POSTGRES.getPassword(),
                "quarkus.datasource.db-kind",   "postgresql"
        );
    }

    /** Stops the container after all tests using this resource have finished. */
    @Override
    public void stop() {
        POSTGRES.stop();
    }
}
