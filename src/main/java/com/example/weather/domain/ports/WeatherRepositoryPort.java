// Output port (interface) abstracting the persistence layer for weather readings.
//   save()       — persists a new reading and returns it with its assigned DB id.
//   findByCity() — retrieves all readings for a city, newest first.
//   findLatest() — returns the single most recent reading wrapped in Optional.
// Implemented by WeatherRepositoryAdapter; easy to stub in unit tests.
package com.example.weather.domain.ports;

import com.example.weather.domain.model.WeatherReading;

import java.util.List;
import java.util.Optional;

/**
 * Output port for weather reading persistence.
 * The domain layer depends only on this interface; the JPA/Panache implementation
 * ({@code WeatherRepositoryAdapter}) lives in the adapters layer and can be swapped or mocked freely.
 */
public interface WeatherRepositoryPort {

    /**
     * Persists a weather reading and returns it with its DB-assigned id.
     *
     * @param reading the reading to save (id should be null)
     * @return the saved reading with a non-null id
     */
    WeatherReading save(WeatherReading reading);

    /**
     * Returns all stored readings for a city, ordered newest first.
     *
     * @param city city name to filter by
     * @return list of readings (may be empty, never null)
     */
    List<WeatherReading> findByCity(String city);

    /**
     * Returns the single most recent reading for a city.
     *
     * @param city city name to filter by
     * @return an {@link Optional} containing the latest reading, or empty if none exist
     */
    Optional<WeatherReading> findLatest(String city);
}
