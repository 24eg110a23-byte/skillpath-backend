package com.skillpath.dto;

/**
 * A generic "label + number" bucket used by dashboard charts.
 * The key is the raw enum name (or category string), the label is human readable.
 */
public record GroupCount(
        String key,
        String label,
        long count
) {
}
