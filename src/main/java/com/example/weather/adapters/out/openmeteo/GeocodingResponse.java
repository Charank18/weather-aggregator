// POJO that maps the JSON body returned by the Open-Meteo geocoding endpoint (/v1/search).
// Contains a list of Result objects; each result holds the city name plus its lat/lon.
// OpenMeteoAdapter reads the first result to produce a Coordinates value object.
package com.example.weather.adapters.out.openmeteo;

import java.util.List;

/**
 * Deserialised response from the Open-Meteo geocoding API.
 * Top-level wrapper whose {@code results} list contains candidate city matches.
 */
public class GeocodingResponse {

    /** List of matching cities; may be null or empty when the city is not found. */
    public List<Result> results;

    /**
     * A single city match returned by the geocoding API.
     */
    public static class Result {

        /** Latitude of the city centre in decimal degrees. */
        public double latitude;

        /** Longitude of the city centre in decimal degrees. */
        public double longitude;

        /** Display name of the city as returned by the API. */
        public String name;
    }
}
