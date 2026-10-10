package com.skillpath.dto;

import com.skillpath.domain.Relevance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload used to attach an existing course to a skill.
 */
public record LinkCourseRequest(

        @NotNull(message = "Course id is required")
        Long courseId,

        Relevance relevance,

        @Size(max = 500, message = "Notes must be at most 500 characters")
        String notes
) {
}
