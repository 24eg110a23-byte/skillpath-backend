package com.skillpath.error;

/**
 * Raised when a create/update would violate a unique business key
 * (skill name, course title, or a duplicate skill-course link).
 * Translated to HTTP 409 by {@link GlobalExceptionHandler}.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
