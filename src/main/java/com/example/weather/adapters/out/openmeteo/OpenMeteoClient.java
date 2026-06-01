// MicroProfile REST Client interface for the Open-Meteo weather forecast API.
// Registered under config key "open-meteo"; base URL set in application.properties.
//   geocode() — GET /v1/search   — city-name to coordinates lookup
//   weather() — GET /v1/forecast — current weather snapshot for a lat/lon pair
package com.example.weather.adapters.out.openmeteo;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * MicroProfile REST Client for the Open-Meteo forecast API.
 * Base URL is bound via {@code quarkus.rest-client.open-meteo.url} in application.properties.
 */
@Path("/")
@RegisterRestClient(configKey = "open-meteo")
public interface OpenMeteoClient {

    /**
     * Geocoding lookup — resolves a city name to coordinates.
     *
     * @param city     city name to search for
     * @param count    maximum number of results (typically 1)
     * @param language language code for display names (e.g. "en")
     * @param format   response format — must be "json"
     * @return matching cities with latitude, longitude, and display name
     */
    @GET
    @Path("/v1/search")
    GeocodingResponse geocode(
            @QueryParam("name") String city,
            @QueryParam("count") int count,
            @QueryParam("language") String language,
            @QueryParam("format") String format
    );

    /**
     * Forecast endpoint — returns the current weather for a coordinate pair.
     *
     * @param latitude       decimal latitude
     * @param longitude      decimal longitude
     * @param currentWeather {@code true} to include the {@code current_weather} block
     * @param unit           wind speed unit, e.g. {@code "kmh"}
     * @return current weather containing temperature, wind speed, and WMO weather code
     */
    @GET
    @Path("/v1/forecast")
    WeatherResponse weather(
            @QueryParam("latitude") double latitude,
            @QueryParam("longitude") double longitude,
            @QueryParam("current_weather") boolean currentWeather,
            @QueryParam("wind_speed_unit") String unit
    );
}
