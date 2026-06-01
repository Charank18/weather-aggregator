// Simple JSON error envelope returned by all exception mappers.
// Serialises to {"error": "<message>"} so every error response has a consistent shape.
package com.example.weather.adapters.in.rest;

/**
 * Uniform error body returned for all 4xx / 5xx responses.
 * Serialises to {@code {"error": "..."}}.
 *
 * @param error human-readable description of what went wrong
 */
public record ErrorResponse(String error) {
}
