// POJO that maps the JSON body returned by the Open-Meteo forecast endpoint (/v1/forecast).
// The nested CurrentWeather class holds temperature (°C), wind speed (km/h),
// and the raw WMO integer weather code used by WeatherCodeMapper.
package com.example.weather.adapters.out.openmeteo;

/**
 * Deserialised response from the Open-Meteo forecast API.
 * Only the {@code current_weather} block is used; other forecast fields are ignored.
 */
public class WeatherResponse {

    /** Current weather conditions block from the API response. */
    public CurrentWeather current_weather;

    /**
     * The {@code current_weather} object nested inside the forecast response.
     */
    public static class CurrentWeather {

        /** Air temperature at 2 m above ground in degrees Celsius. */
        public double temperature;

        /** Wind speed at 10 m above ground in km/h. */
        public double windspeed;

        /**
         * WMO Weather Interpretation Code (WW).
         * Mapped to a human-readable string by {@code WeatherCodeMapper}.
         */
        public int weathercode;
    }
}
