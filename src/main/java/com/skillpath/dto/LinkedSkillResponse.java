package com.skillpath.dto;

import java.time.Instant;

import com.skillpath.domain.Relevance;

/**
 * A skill as seen from the perspective of a course it is attached to.
 */
public record LinkedSkillResponse(
        Long linkId,
        Relevance relevance,
        String relevanceLabel,
        String notes,
        Instant linkedAt,
        SkillResponse skill
) {
}
