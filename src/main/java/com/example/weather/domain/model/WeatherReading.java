// Core domain model representing a single weather snapshot for a city.
// Fields: id (DB primary key, null before persistence), city name, temperature (°C),
// wind speed (km/h), human-readable weather description, and the UTC fetch timestamp.
// Plain POJO — no framework annotations — so it can be used and tested independently
// of JPA, CDI, or JSON libraries.
package com.example.weather.domain.model;

import java.time.LocalDateTime;

/**
 * Immutable-by-convention domain model for a single weather snapshot.
 * Created by {@code WeatherService.fetchAndStore()}, persisted via the repository port,
 * and returned directly as the HTTP response body (serialised to JSON by Quarkus).
 */
public class WeatherReading {

    /** Database primary key; {@code null} before the entity is persisted. */
    private Long id;

    /** Name of the city this reading was fetched for. */
    private String city;

    /** Air temperature in degrees Celsius. */
    private double temperature;

    /** Wind speed in km/h. */
    private double windSpeed;

    /**
     * Human-readable weather condition derived from the WMO weather code,
     * e.g. {@code "Overcast"}, {@code "Rain: slight"}.
     */
    private String weatherDescription;

    /** UTC timestamp of when this reading was fetched from the upstream API. */
    private LocalDateTime fetchedAt;

    /** No-arg constructor required for JSON deserialisation. */
    public WeatherReading() {
    }

    /**
     * Full constructor used by {@code WeatherService} (before persistence, id is null)
     * and by {@code WeatherRepositoryAdapter} (after persistence, id is set).
     */
    public WeatherReading(
            Long id,
            String city,
            double temperature,
            double windSpeed,
            String weatherDescription,
            LocalDateTime fetchedAt
    ) {
        this.id = id;
        this.city = city;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.weatherDescription = weatherDescription;
        this.fetchedAt = fetchedAt;
    }

    /** @return DB-assigned primary key, or {@code null} if not yet persisted */
    public Long getId() {
        return id;
    }

    /** @return city name this reading belongs to */
    public String getCity() {
        return city;
    }

    /** @return temperature in degrees Celsius */
    public double getTemperature() {
        return temperature;
    }

    /** @return wind speed in km/h */
    public double getWindSpeed() {
        return windSpeed;
    }

    /** @return human-readable weather description (e.g. "Overcast") */
    public String getWeatherDescription() {
        return weatherDescription;
    }

    /** @return UTC timestamp of when the reading was fetched */
    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }
}
