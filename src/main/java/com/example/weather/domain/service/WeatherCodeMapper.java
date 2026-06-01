// Utility that translates WMO Weather Interpretation Codes (WW) into human-readable strings.
// Covers the subset of codes returned by Open-Meteo (clear sky through thunderstorms with hail).
// map(int code) returns the matching description, or "Unknown" for any unrecognised code.
// The lookup map is built once at class-load time from an immutable Map.ofEntries() call.
package com.example.weather.domain.service;

import java.util.Map;

/**
 * Translates WMO Weather Interpretation Codes (WW) into human-readable descriptions.
 * Covers the subset of codes that Open-Meteo returns (0–99).
 *
 * <p>Reference: <a href="https://open-meteo.com/en/docs">Open-Meteo documentation</a></p>
 */
public class WeatherCodeMapper {

    /**
     * Immutable lookup table from WMO code → description.
     * Built once at class-load time; never mutated.
     */
    private static final Map<Integer, String> MAP = Map.ofEntries(
            Map.entry(0,  "Clear sky"),
            Map.entry(1,  "Mainly clear"),
            Map.entry(2,  "Partly cloudy"),
            Map.entry(3,  "Overcast"),
            Map.entry(45, "Fog"),
            Map.entry(48, "Depositing rime fog"),
            Map.entry(51, "Drizzle: light"),
            Map.entry(53, "Drizzle: moderate"),
            Map.entry(55, "Drizzle: dense"),
            Map.entry(56, "Freezing drizzle: light"),
            Map.entry(57, "Freezing drizzle: dense"),
            Map.entry(61, "Rain: slight"),
            Map.entry(63, "Rain: moderate"),
            Map.entry(65, "Rain: heavy"),
            Map.entry(66, "Freezing rain: light"),
            Map.entry(67, "Freezing rain: heavy"),
            Map.entry(71, "Snow fall: slight"),
            Map.entry(73, "Snow fall: moderate"),
            Map.entry(75, "Snow fall: heavy"),
            Map.entry(77, "Snow grains"),
            Map.entry(80, "Rain showers: slight"),
            Map.entry(81, "Rain showers: moderate"),
            Map.entry(82, "Rain showers: violent"),
            Map.entry(85, "Snow showers: slight"),
            Map.entry(86, "Snow showers: heavy"),
            Map.entry(95, "Thunderstorm: slight or moderate"),
            Map.entry(96, "Thunderstorm with slight hail"),
            Map.entry(99, "Thunderstorm with heavy hail")
    );

    /**
     * Returns the human-readable description for a WMO weather code.
     *
     * @param code WMO Weather Interpretation Code as returned by Open-Meteo
     * @return description string, or {@code "Unknown"} for unrecognised codes
     */
    public static String map(int code) {
        return MAP.getOrDefault(code, "Unknown");
    }
}
