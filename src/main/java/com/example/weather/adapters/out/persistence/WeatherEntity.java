// JPA entity that maps to the "weatherentity" table in PostgreSQL.
// Extends PanacheEntity for an auto-managed Long primary key (id).
// Stores city name, temperature (°C), wind speed (km/h), human-readable weather
// description, and the timestamp when the reading was fetched.
// An index on the city column (idx_weather_city) speeds up per-city queries.
package com.example.weather.adapters.out.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA entity representing a persisted weather reading.
 * The inherited {@code id} field (from {@link PanacheEntity}) is the auto-generated primary key.
 */
@Entity
@Table(indexes = @Index(name = "idx_weather_city", columnList = "city"))
public class WeatherEntity extends PanacheEntity {

    /** Name of the city this reading belongs to. Used in all per-city queries. */
    public String city;

    /** Air temperature in degrees Celsius at the time of the reading. */
    public double temperature;

    /** Wind speed in km/h at the time of the reading. */
    public double windSpeed;

    /** Human-readable weather description derived from the WMO weather code (e.g. "Overcast"). */
    public String weatherDescription;

    /** UTC timestamp of when this reading was fetched from the upstream API. */
    public LocalDateTime fetchedAt;
}
