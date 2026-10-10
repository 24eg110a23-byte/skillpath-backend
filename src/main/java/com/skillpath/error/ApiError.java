package com.skillpath.error;

import java.time.Instant;
import java.util.List;

/**
 * Uniform error body returned by every failing endpoint.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> fieldErrors
) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiError of(int status, String error, String message, String path,
                              List<FieldViolation> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, path,
                fieldErrors == null ? List.of() : fieldErrors);
    }

    /**
     * One validation failure tied to a specific request field.
     */
    public record FieldViolation(String field, String message) {
    }
}
