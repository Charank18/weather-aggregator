// JAX-RS ExceptionMapper that catches CityNotFoundException thrown anywhere in the request
// pipeline and converts it into a 404 Not Found JSON response using ErrorResponse as the body.
package com.example.weather.adapters.in.rest;

import com.example.weather.domain.exceptions.CityNotFoundException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Converts {@link CityNotFoundException} into an HTTP 404 Not Found response.
 * Registered automatically by JAX-RS via the {@code @Provider} annotation.
 */
@Provider
public class CityNotFoundExceptionMapper
        implements ExceptionMapper<CityNotFoundException> {

    /**
     * Builds a 404 response whose JSON body contains the exception message.
     *
     * @param exception the exception carrying the "City not found: X" message
     * @return 404 response with an {@link ErrorResponse} body
     */
    @Override
    public Response toResponse(CityNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
