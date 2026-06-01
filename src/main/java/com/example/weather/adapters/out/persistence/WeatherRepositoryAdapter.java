// Outbound persistence adapter implementing WeatherRepositoryPort via Panache (JPA).
//   save()       — maps a WeatherReading to a WeatherEntity, persists it, returns the
//                  reading back with the DB-assigned id populated.
//   findByCity() — retrieves all readings for a city ordered newest first.
//   findLatest() — returns only the single most recent reading wrapped in Optional.
// The private map() helper converts WeatherEntity rows back into domain WeatherReading objects,
// keeping JPA details out of the domain layer.
package com.example.weather.adapters.out.persistence;

import com.example.weather.domain.model.WeatherReading;
import com.example.weather.domain.ports.WeatherRepositoryPort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Panache-backed implementation of {@link WeatherRepositoryPort}.
 * All public methods run inside a transaction (class-level {@code @Transactional}).
 */
@ApplicationScoped
@Transactional
public class WeatherRepositoryAdapter implements WeatherRepositoryPort {

    /**
     * Persists a new weather reading and returns it with its DB-assigned id.
     *
     * @param reading the domain reading to persist (id must be null)
     * @return the same reading with the generated id populated
     */
    @Override
    public WeatherReading save(WeatherReading reading) {

        // Map domain object → JPA entity
        WeatherEntity entity = new WeatherEntity();
        entity.city = reading.getCity();
        entity.temperature = reading.getTemperature();
        entity.windSpeed = reading.getWindSpeed();
        entity.weatherDescription = reading.getWeatherDescription();
        entity.fetchedAt = reading.getFetchedAt();

        // Panache assigns the id on persist; map back so the caller gets it
        entity.persist();

        return map(entity);
    }

    /**
     * Returns all stored readings for a city, ordered newest first.
     *
     * @param city city name to filter by
     * @return list of readings (may be empty)
     */
    @Override
    public List<WeatherReading> findByCity(String city) {

        return WeatherEntity
                .<WeatherEntity>find("city = ?1 order by fetchedAt desc", city)
                .stream()
                .map(this::map)
                .toList();
    }

    /**
     * Returns the single most recent reading for a city, or empty if none exist.
     *
     * @param city city name to filter by
     * @return an {@link Optional} containing the latest reading, or empty
     */
    @Override
    public Optional<WeatherReading> findLatest(String city) {

        WeatherEntity entity = WeatherEntity.find(
                "city = ?1 order by fetchedAt desc",
                city
        ).firstResult();

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(map(entity));
    }

    /**
     * Converts a {@link WeatherEntity} row into a domain {@link WeatherReading}.
     * Keeps all JPA-specific types (entity ids, column names) out of the domain layer.
     *
     * @param entity the JPA entity to convert
     * @return a fully populated domain reading
     */
    private WeatherReading map(WeatherEntity entity) {

        return new WeatherReading(
                entity.id,
                entity.city,
                entity.temperature,
                entity.windSpeed,
                entity.weatherDescription,
                entity.fetchedAt
        );
    }
}
