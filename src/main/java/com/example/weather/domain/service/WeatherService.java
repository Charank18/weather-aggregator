// Core domain service orchestrating the weather-fetch workflow.
//   fetchAndStore(city) — validates the city name, resolves it to Coordinates via the
//                         provider port, fetches current weather, maps the WMO code to
//                         a description, builds a WeatherReading, and persists it.
//   getByCity(city)     — returns all stored readings for a city via the repository port.
//   getLatest(city)     — returns only the most recent stored reading.
// All methods throw IllegalArgumentException for a blank/null city.
// No framework annotations — constructed via AppConfig for clean unit testability.
package com.example.weather.domain.service;

import com.example.weather.domain.model.WeatherReading;
import com.example.weather.domain.ports.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Core domain service for the weather-aggregator application.
 * Orchestrates the two outbound ports — {@link WeatherProviderPort} (HTTP)
 * and {@link WeatherRepositoryPort} (database) — to fulfil the three use cases.
 *
 * <p>This class has no CDI or JPA annotations; it is instantiated by {@code AppConfig}
 * so it can be constructed and tested without a running container.</p>
 */
public class WeatherService {

    private final WeatherProviderPort provider;
    private final WeatherRepositoryPort repository;

    /**
     * @param provider   outbound port for fetching live weather data
     * @param repository outbound port for persisting and querying weather readings
     */
    public WeatherService(
            WeatherProviderPort provider,
            WeatherRepositoryPort repository
    ) {
        this.provider = provider;
        this.repository = repository;
    }

    /**
     * Fetches the current weather for a city from the external API and stores it.
     *
     * <ol>
     *   <li>Validates that {@code city} is non-blank.</li>
     *   <li>Resolves the city name to geographic coordinates.</li>
     *   <li>Fetches the current weather snapshot for those coordinates.</li>
     *   <li>Translates the raw WMO code into a human-readable description.</li>
     *   <li>Builds and persists a {@link WeatherReading}.</li>
     * </ol>
     *
     * @param city city name (e.g. "London")
     * @return the persisted reading with its DB-assigned id
     * @throws IllegalArgumentException   if {@code city} is null or blank
     * @throws com.example.weather.domain.exceptions.CityNotFoundException if the city cannot be resolved
     * @throws com.example.weather.domain.exceptions.RateLimitExceededException if an upstream API rate-limits the request
     */
    public WeatherReading fetchAndStore(String city) {

        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }

        // Trim once; use normalised form throughout
        String normalizedCity = city.trim();

        // Step 1 — resolve city name → lat/lon
        Coordinates coordinates = provider.resolveCity(normalizedCity);

        // Step 2 — fetch current weather for those coordinates
        CurrentWeather weather = provider.fetchCurrentWeather(
                coordinates.latitude(),
                coordinates.longitude()
        );

        // Step 3 — map the raw WMO integer code to a readable string
        String description = WeatherCodeMapper.map(weather.weatherCode());

        // Step 4 — assemble the domain object and persist
        WeatherReading reading = new WeatherReading(
                null,           // id assigned by the DB
                normalizedCity,
                weather.temperature(),
                weather.windSpeed(),
                description,
                LocalDateTime.now()
        );

        return repository.save(reading);
    }

    /**
     * Returns all stored weather readings for a city, ordered newest first.
     *
     * @param city city name to query
     * @return list of readings (may be empty)
     * @throws IllegalArgumentException if {@code city} is null or blank
     */
    public List<WeatherReading> getByCity(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        return repository.findByCity(city.trim());
    }

    /**
     * Returns the most recent stored weather reading for a city.
     *
     * @param city city name to query
     * @return an {@link Optional} containing the latest reading, or empty if none exist
     * @throws IllegalArgumentException if {@code city} is null or blank
     */
    public Optional<WeatherReading> getLatest(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        return repository.findLatest(city.trim());
    }
}
