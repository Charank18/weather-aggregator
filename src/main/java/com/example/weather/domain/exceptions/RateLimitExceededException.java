// Domain exception thrown when an upstream API (geocoding or forecast) returns HTTP 429.
// Caught by RateLimitExceededExceptionMapper and returned to the client as HTTP 429.
package com.example.weather.domain.exceptions;

/**
 * Thrown when an upstream API (geocoding or forecast) responds with HTTP 429 Too Many Requests.
 * The message identifies which service hit the limit, e.g.
 * {@code "Geocoding service rate limit exceeded. Please try again later."}.
 */
public class RateLimitExceededException extends RuntimeException {

    /**
     * @param message description of which upstream service enforced the rate limit
     */
    public RateLimitExceededException(String message) {
        super(message);
    }
}
