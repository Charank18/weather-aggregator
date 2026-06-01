// CDI configuration class that manually wires the domain service.
// Produces the WeatherService bean by injecting WeatherProviderPort and
// WeatherRepositoryPort, keeping WeatherService free of CDI annotations
// and straightforwardly testable without a container.
package com.example.weather.config;

import com.example.weather.domain.ports.WeatherProviderPort;
import com.example.weather.domain.ports.WeatherRepositoryPort;
import com.example.weather.domain.service.WeatherService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * CDI producer configuration that wires the domain layer.
 * By constructing {@link WeatherService} manually here (rather than annotating it with
 * {@code @ApplicationScoped}), the service stays free of any framework dependency and
 * can be instantiated directly in unit tests.
 */
@ApplicationScoped
public class AppConfig {

    /**
     * Produces the application-scoped {@link WeatherService} by injecting both port
     * implementations resolved by CDI.
     *
     * @param provider   the outbound adapter that fetches weather from Open-Meteo
     * @param repository the outbound adapter that reads/writes weather to PostgreSQL
     * @return a fully wired {@link WeatherService}
     */
    @Produces
    public WeatherService weatherService(
            WeatherProviderPort provider,
            WeatherRepositoryPort repository
    ) {
        return new WeatherService(provider, repository);
    }
}
