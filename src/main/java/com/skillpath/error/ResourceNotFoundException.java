package com.skillpath.error;

/**
 * Raised when an entity referenced by id (or by a unique business key) does not exist.
 * Translated to HTTP 404 by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forEntity(String entity, Long id) {
        return new ResourceNotFoundException(entity + " with id " + id + " was not found");
    }

    public static ResourceNotFoundException forName(String entity, String name) {
        return new ResourceNotFoundException(entity + " named '" + name + "' was not found");
    }
}
