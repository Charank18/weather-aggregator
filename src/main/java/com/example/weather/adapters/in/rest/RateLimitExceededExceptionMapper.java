// JAX-RS ExceptionMapper that converts RateLimitExceededException into HTTP 429
// (Too Many Requests) with a JSON error body, surfacing upstream rate-limit errors to clients.
package com.example.weather.adapters.in.rest;

import com.example.weather.domain.exceptions.RateLimitExceededException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Converts {@link RateLimitExceededException} into an HTTP 429 Too Many Requests response.
 * Registered automatically by JAX-RS via {@code @Provider}.
 */
@Provider
public class RateLimitExceededExceptionMapper
        implements ExceptionMapper<RateLimitExceededException> {

    /**
     * Builds a 429 response whose JSON body contains the rate-limit message.
     *
     * @param exception the exception carrying the upstream rate-limit description
     * @return 429 response with an {@link ErrorResponse} body
     */
    @Override
    public Response toResponse(RateLimitExceededException exception) {
        // 429 Too Many Requests — not in the JAX-RS Status enum, so use the raw int
        return Response.status(429)
                .entity(new ErrorResponse(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
