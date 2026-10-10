package com.skillpath.dto;

import java.time.Instant;

import com.skillpath.domain.Relevance;

/**
 * A course as seen from the perspective of the skill it is attached to.
 */
public record LinkedCourseResponse(
        Long linkId,
        Relevance relevance,
        String relevanceLabel,
        String notes,
        Instant linkedAt,
        CourseResponse course
) {
}
