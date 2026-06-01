// Immutable value object (record) representing a geographic position (latitude, longitude).
// Returned by WeatherProviderPort.resolveCity() and passed straight into
// WeatherProviderPort.fetchCurrentWeather() inside WeatherService.
package com.example.weather.domain.ports;

/**
 * Geographic coordinates of a city.
 * Passed from {@code WeatherProviderPort.resolveCity()} directly into
 * {@code WeatherProviderPort.fetchCurrentWeather()} so the service never
 * handles raw doubles for position.
 *
 * @param latitude  decimal latitude (positive = north)
 * @param longitude decimal longitude (positive = east)
 */
public record Coordinates(
        double latitude,
        double longitude
) {
}
