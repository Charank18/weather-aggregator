// Immutable value object (record) carrying raw weather data from the provider.
// Holds temperature (°C), wind speed (km/h), and the raw WMO integer weather code.
// WeatherService maps the weather code to a human-readable string via WeatherCodeMapper.
package com.example.weather.domain.ports;

/**
 * Raw weather snapshot returned by {@code WeatherProviderPort.fetchCurrentWeather()}.
 * {@code WeatherService} converts {@code weatherCode} to a human-readable description
 * via {@code WeatherCodeMapper} before building the final {@code WeatherReading}.
 *
 * @param temperature air temperature in degrees Celsius
 * @param windSpeed   wind speed in km/h
 * @param weatherCode WMO Weather Interpretation Code (WW) integer
 */
public record CurrentWeather(
        double temperature,
        double windSpeed,
        int weatherCode
) {
}
