// Outbound adapter implementing WeatherProviderPort against the Open-Meteo public APIs.
//   resolveCity()         — calls the geocoding API to convert a city name to Coordinates;
//                           throws CityNotFoundException when the API returns no results.
//   fetchCurrentWeather() — calls the forecast API for the given lat/lon and returns
//                           temperature, wind speed, and WMO weather code.
// HTTP 429 from either API is translated into RateLimitExceededException; other HTTP
// errors are re-thrown as-is (WebApplicationException).
package com.example.weather.adapters.out.openmeteo;

import com.example.weather.domain.exceptions.CityNotFoundException;
import com.example.weather.domain.exceptions.RateLimitExceededException;
import com.example.weather.domain.ports.Coordinates;
import com.example.weather.domain.ports.CurrentWeather;
import com.example.weather.domain.ports.WeatherProviderPort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Outbound adapter that satisfies {@link WeatherProviderPort} by calling the
 * Open-Meteo geocoding and forecast REST APIs via MicroProfile REST Client proxies.
 */
@ApplicationScoped
public class OpenMeteoAdapter implements WeatherProviderPort {

    private final GeocodingClient geocodingClient;
    private final OpenMeteoClient openMeteoClient;

    @Inject
    public OpenMeteoAdapter(
            @RestClient GeocodingClient geocodingClient,
            @RestClient OpenMeteoClient openMeteoClient
    ) {
        this.geocodingClient = geocodingClient;
        this.openMeteoClient = openMeteoClient;
    }

    /**
     * Resolves a city name to geographic coordinates via the geocoding API.
     *
     * @param city city name to look up
     * @return {@link Coordinates} for the best-matching city
     * @throws CityNotFoundException      if the API returns no results for the city
     * @throws RateLimitExceededException if the geocoding API responds with HTTP 429
     */
    @Override
    public Coordinates resolveCity(String city) {

        try {
            GeocodingResponse response =
                    geocodingClient.geocode(city, 1, "en", "json");

            // Empty results mean the city name is not recognised
            if (response.results == null || response.results.isEmpty()) {
                throw new CityNotFoundException(city);
            }

            // Take the first (best) match
            GeocodingResponse.Result result = response.results.get(0);

            return new Coordinates(result.latitude, result.longitude);

        } catch (CityNotFoundException e) {
            // Re-throw domain exception without wrapping
            throw e;
        } catch (WebApplicationException e) {
            throw mapClientException(e, "Geocoding service");
        }
    }

    /**
     * Fetches the current weather snapshot for the given coordinates via the forecast API.
     *
     * @param latitude  decimal latitude
     * @param longitude decimal longitude
     * @return {@link CurrentWeather} with temperature, wind speed, and WMO weather code
     * @throws RateLimitExceededException if the forecast API responds with HTTP 429
     */
    @Override
    public CurrentWeather fetchCurrentWeather(double latitude, double longitude) {

        try {
            WeatherResponse response =
                    openMeteoClient.weather(latitude, longitude, true, "kmh");

            return new CurrentWeather(
                    response.current_weather.temperature,
                    response.current_weather.windspeed,
                    response.current_weather.weathercode
            );

        } catch (WebApplicationException e) {
            throw mapClientException(e, "Weather service");
        }
    }

    /**
     * Maps a {@link WebApplicationException} to a domain exception where applicable.
     * HTTP 429 → {@link RateLimitExceededException}; all other statuses are re-thrown as-is.
     *
     * @param exception  the HTTP error from the REST client
     * @param serviceName label used in the rate-limit error message (e.g. "Geocoding service")
     * @return a {@link RuntimeException} to throw
     */
    private RuntimeException mapClientException(
            WebApplicationException exception,
            String serviceName
    ) {
        int status = exception.getResponse().getStatus();

        if (status == 429) {
            return new RateLimitExceededException(
                    serviceName + " rate limit exceeded. Please try again later."
            );
        }

        // For all other HTTP errors pass the original exception up the stack
        return exception;
    }
}
