// Output port (interface) abstracting the external weather data source.
//   resolveCity()         — converts a city name into geographic Coordinates.
//   fetchCurrentWeather() — retrieves the current weather snapshot for a lat/lon pair.
// Implemented by OpenMeteoAdapter; swappable or mockable in tests without touching domain logic.
package com.example.weather.domain.ports;

/**
 * Output port for the external weather data source.
 * The domain layer depends only on this interface; the concrete HTTP implementation
 * ({@code OpenMeteoAdapter}) lives in the adapters layer and can be swapped or mocked freely.
 */
public interface WeatherProviderPort {

    /**
     * Resolves a city name to geographic coordinates.
     *
     * @param city city name to look up (e.g. "London")
     * @return {@link Coordinates} for the best match
     * @throws com.example.weather.domain.exceptions.CityNotFoundException if the city is not found
     * @throws com.example.weather.domain.exceptions.RateLimitExceededException if the API rate-limits the request
     */
    Coordinates resolveCity(String city);

    /**
     * Fetches the current weather snapshot for the given coordinates.
     *
     * @param latitude  decimal latitude
     * @param longitude decimal longitude
     * @return {@link CurrentWeather} with temperature, wind speed, and WMO weather code
     * @throws com.example.weather.domain.exceptions.RateLimitExceededException if the API rate-limits the request
     */
    CurrentWeather fetchCurrentWeather(
            double latitude,
            double longitude
    );
}
