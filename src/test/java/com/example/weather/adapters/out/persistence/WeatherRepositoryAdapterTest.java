// Integration tests for WeatherRepositoryAdapter against a real PostgreSQL instance
// spun up via Testcontainers (PostgresTestResource).
// Verifies three behaviours:
//   - a saved reading gets a non-null DB id and is retrievable by city
//   - findByCity() returns readings in newest-first order
//   - findLatest() returns only the single most recent reading with correct values
package com.example.weather.adapters.out.persistence;

import com.example.weather.domain.model.WeatherReading;
import com.example.weather.support.PostgresTestResource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link WeatherRepositoryAdapter}.
 * Runs a full Quarkus application against a throwaway PostgreSQL container
 * provided by {@link PostgresTestResource}.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class WeatherRepositoryAdapterTest {

    @Inject
    WeatherRepositoryAdapter repository;

    /**
     * Saves a reading and immediately retrieves it by city.
     * Asserts the DB assigns a non-null id and the city name round-trips correctly.
     */
    @Test
    @Transactional
    void shouldSaveAndRetrieveWeatherReading() {

        WeatherReading reading = new WeatherReading(
                null, "London", 25.5, 12.4, "Overcast", LocalDateTime.now()
        );

        WeatherReading saved = repository.save(reading);

        // DB must have assigned a primary key
        assertNotNull(saved.getId());

        List<WeatherReading> readings = repository.findByCity("London");
        assertFalse(readings.isEmpty());
        assertEquals("London", readings.get(0).getCity());
    }

    /**
     * Saves two readings for the same city with different timestamps and checks
     * that {@code findByCity} returns them in newest-first order.
     */
    @Test
    @Transactional
    void shouldReturnReadingsOrderedMostRecentFirst() {

        LocalDateTime older = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime newer = LocalDateTime.of(2026, 5, 31, 14, 0);

        repository.save(new WeatherReading(null, "Paris", 10.0, 5.0, "Clear sky", older));
        repository.save(new WeatherReading(null, "Paris", 12.0, 6.0, "Overcast", newer));

        List<WeatherReading> readings = repository.findByCity("Paris");

        assertEquals(2, readings.size());
        // Newest entry must be first
        assertEquals(newer, readings.get(0).getFetchedAt());
        assertEquals(older, readings.get(1).getFetchedAt());
    }

    /**
     * Saves two readings for the same city and checks that {@code findLatest}
     * returns only the more recent one with the correct field values.
     */
    @Test
    @Transactional
    void shouldReturnLatestReading() {

        LocalDateTime older = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime newer = LocalDateTime.of(2026, 5, 31, 14, 0);

        repository.save(new WeatherReading(null, "Berlin", 8.0, 3.0, "Clear sky", older));
        repository.save(new WeatherReading(null, "Berlin", 9.0, 4.0, "Overcast", newer));

        WeatherReading latest = repository.findLatest("Berlin").orElseThrow();

        assertEquals(newer, latest.getFetchedAt());
        assertEquals(9.0, latest.getTemperature());
    }
}
