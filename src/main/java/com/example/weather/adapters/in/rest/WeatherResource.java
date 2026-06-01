// REST controller (inbound adapter) exposing the weather API under /weather.
//   POST /weather/fetch?city=   — triggers a live fetch from Open-Meteo and persists the result
//   GET  /weather/{city}        — returns all stored readings for a city, newest first
//   GET  /weather/{city}/latest — returns the single most recent stored reading for a city
// Delegates all business logic to WeatherService; returns JSON for every response.
package com.example.weather.adapters.in.rest;

import com.example.weather.domain.service.WeatherService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
public class WeatherResource {

    @Inject
    WeatherService service;

    /**
     * Fetches live weather for the given city from Open-Meteo, persists it,
     * and returns the saved reading.
     *
     * @param city city name (e.g. "London")
     * @return the persisted {@code WeatherReading}
     */
    @POST
    @Path("/fetch")
    public Object fetchWeather(@QueryParam("city") String city) {
        return service.fetchAndStore(city);
    }

    /**
     * Returns all stored weather readings for a city, ordered newest first.
     *
     * @param city city name
     * @return list of {@code WeatherReading} (may be empty)
     */
    @GET
    @Path("/{city}")
    public Object getAllReadings(@PathParam("city") String city) {
        return service.getByCity(city);
    }

    /**
     * Returns only the most recent stored weather reading for a city.
     *
     * @param city city name
     * @return the latest {@code WeatherReading}, or 404 if none exist
     */
    @GET
    @Path("/{city}/latest")
    public Object getLatestReading(@PathParam("city") String city) {
        return service.getLatest(city);
    }
}
