// MicroProfile REST Client interface for the Open-Meteo geocoding API (/v1/search).
// Registered under config key "geocoding-api"; base URL set in application.properties.
// Used by OpenMeteoAdapter to resolve a city name to latitude/longitude coordinates.
package com.example.weather.adapters.out.openmeteo;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * MicroProfile REST Client for the Open-Meteo geocoding endpoint.
 * Base URL is bound via {@code quarkus.rest-client.geocoding-api.url} in application.properties.
 */
@Path("/")
@RegisterRestClient(configKey = "geocoding-api")
public interface GeocodingClient {

    /**
     * Searches for a city by name and returns the top match with its coordinates.
     *
     * @param city     city name to look up (e.g. "London")
     * @param count    maximum number of results to return (typically 1)
     * @param language language code for result names (e.g. "en")
     * @param format   response format (must be "json")
     * @return geocoding results including latitude, longitude, and display name
     */
    @GET
    @Path("/v1/search")
    GeocodingResponse geocode(
            @QueryParam("name") String city,
            @QueryParam("count") int count,
            @QueryParam("language") String language,
            @QueryParam("format") String format
    );
}
