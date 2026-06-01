// JAX-RS ExceptionMapper that converts IllegalArgumentException (e.g. blank city name)
// into a 400 Bad Request JSON response so validation errors reach the caller cleanly.
package com.example.weather.adapters.in.rest;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Converts {@link IllegalArgumentException} (e.g. blank city name) into an HTTP 400
 * Bad Request response. Registered automatically by JAX-RS via {@code @Provider}.
 */
@Provider
public class IllegalArgumentExceptionMapper
        implements ExceptionMapper<IllegalArgumentException> {

    /**
     * Builds a 400 response whose JSON body contains the validation message.
     *
     * @param exception the exception carrying the validation error message
     * @return 400 response with an {@link ErrorResponse} body
     */
    @Override
    public Response toResponse(IllegalArgumentException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
