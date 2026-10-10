package com.skillpath.dto;

import com.skillpath.domain.CourseStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Payload for a focused update of how far along a course is.
 */
public record UpdateCourseProgressRequest(

        @NotNull(message = "Status is required")
        CourseStatus status,

        @NotNull(message = "Completed hours is required")
        @Min(value = 0, message = "Completed hours cannot be negative")
        @Max(value = 10000, message = "Completed hours must be at most 10000")
        Integer completedHours
) {
}
