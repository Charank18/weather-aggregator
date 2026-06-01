// Domain exception thrown when a city name cannot be resolved to coordinates.
// Caught by CityNotFoundExceptionMapper and returned to the client as HTTP 404.
package com.example.weather.domain.exceptions;

/**
 * Thrown when the geocoding API returns no results for the requested city name.
 * Carries the city name in the message: {@code "City not found: <city>"}.
 */
public class CityNotFoundException extends RuntimeException {

    /**
     * @param city the city name that could not be resolved
     */
    public CityNotFoundException(String city) {
        super("City not found: " + city);
    }
}
