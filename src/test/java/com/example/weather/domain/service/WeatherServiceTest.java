// Pure unit tests for WeatherService using Mockito mocks for both ports (no Quarkus container).
// Covers four scenarios:
//   - happy path: city resolved, weather fetched, WMO code mapped, reading saved and returned
//   - CityNotFoundException propagates when the provider cannot find the city
//   - RuntimeException propagates when the forecast API call fails
//   - IllegalArgumentException is thrown for a blank city string
package com.example.weather.domain.service;

import com.example.weather.domain.exceptions.CityNotFoundException;
import com.example.weather.domain.model.WeatherReading;
import com.example.weather.domain.ports.Coordinates;
import com.example.weather.domain.ports.CurrentWeather;
import com.example.weather.domain.ports.WeatherProviderPort;
import com.example.weather.domain.ports.WeatherRepositoryPort;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WeatherService}.
 * Both ports are Mockito mocks; no Quarkus container or database is involved.
 */
class WeatherServiceTest {

    /**
     * Happy path: provider resolves the city and returns weather data,
     * repository saves the reading. Verifies the returned city and
     * description match expectations and that save() was called once.
     */
    @Test
    void shouldFetchAndStoreWeather() {

        WeatherProviderPort provider = mock(WeatherProviderPort.class);
        WeatherRepositoryPort repository = mock(WeatherRepositoryPort.class);
        WeatherService service = new WeatherService(provider, repository);

        // Stub geocoding: London → (51.5, -0.1)
        when(provider.resolveCity("London")).thenReturn(new Coordinates(51.5, -0.1));
        // Stub forecast: return WMO code 3 ("Overcast")
        when(provider.fetchCurrentWeather(51.5, -0.1))
                .thenReturn(new CurrentWeather(25, 14, 3));

        WeatherReading savedReading =
                new WeatherReading(1L, "London", 25, 14, "Overcast", null);
        when(repository.save(any())).thenReturn(savedReading);

        WeatherReading result = service.fetchAndStore("London");

        assertEquals("London", result.getCity());
        assertEquals("Overcast", result.getWeatherDescription());
        verify(repository).save(any()); // ensure the reading was actually persisted
    }

    /**
     * When the provider throws {@link CityNotFoundException}, the service must
     * propagate it without wrapping so the exception mapper can catch it.
     */
    @Test
    void shouldThrowExceptionWhenCityNotFound() {

        WeatherProviderPort provider = mock(WeatherProviderPort.class);
        WeatherRepositoryPort repository = mock(WeatherRepositoryPort.class);
        WeatherService service = new WeatherService(provider, repository);

        when(provider.resolveCity("Unknown"))
                .thenThrow(new CityNotFoundException("Unknown"));

        assertThrows(
                CityNotFoundException.class,
                () -> service.fetchAndStore("Unknown")
        );
    }

    /**
     * When the forecast API throws a generic RuntimeException (e.g. HTTP 500),
     * the service must propagate it unchanged.
     */
    @Test
    void shouldThrowExceptionWhenWeatherApiFails() {

        WeatherProviderPort provider = mock(WeatherProviderPort.class);
        WeatherRepositoryPort repository = mock(WeatherRepositoryPort.class);
        WeatherService service = new WeatherService(provider, repository);

        when(provider.resolveCity("London")).thenReturn(new Coordinates(10, 20));
        when(provider.fetchCurrentWeather(10, 20))
                .thenThrow(new RuntimeException("API failure"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.fetchAndStore("London")
        );

        assertEquals("API failure", exception.getMessage());
    }

    /**
     * A blank city string (whitespace only) must be rejected with
     * {@link IllegalArgumentException} before any port is called.
     */
    @Test
    void shouldRejectBlankCity() {

        WeatherService service = new WeatherService(
                mock(WeatherProviderPort.class),
                mock(WeatherRepositoryPort.class)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.fetchAndStore("   ")
        );
    }
}
