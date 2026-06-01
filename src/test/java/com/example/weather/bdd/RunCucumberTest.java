// JUnit 5 test that bootstraps a full Quarkus application (with WireMock + Postgres via
// CombinedTestResource) and then runs the Cucumber feature file at
// classpath:features/weather.feature using the step definitions in WeatherStepDefinitions.
// A non-zero exit status from the Cucumber CLI fails the test.
package com.example.weather.bdd;

import com.example.weather.support.CombinedTestResource;

import io.cucumber.core.cli.Main;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 entry point for the Cucumber BDD suite.
 * Starts the full Quarkus application (backed by WireMock + Testcontainers Postgres
 * via {@link CombinedTestResource}), then delegates to the Cucumber CLI to execute
 * {@code features/weather.feature} using the glue code in this package.
 */
@QuarkusTest
@QuarkusTestResource(CombinedTestResource.class)
class RunCucumberTest {

    /**
     * Runs all scenarios in weather.feature.
     * Cucumber's CLI returns 0 on success; any other value means one or more
     * scenarios failed, and the assertion converts that into a test failure.
     */
    @Test
    void runWeatherFeature() {
        byte exitStatus = Main.run(
                new String[]{
                        "--glue", "com.example.weather.bdd",  // package containing step definitions
                        "--monochrome",                         // plain-text output (no ANSI colours)
                        "classpath:features/weather.feature"
                },
                getClass().getClassLoader()
        );

        assertEquals(0, exitStatus, "Cucumber scenarios failed");
    }
}
