package com.skillpath.dto;

import java.time.LocalDate;

import com.skillpath.domain.CourseStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload used to create or replace a {@link com.skillpath.domain.Course}.
 */
public record CourseRequest(

        @NotBlank(message = "Course title is required")
        @Size(max = 160, message = "Course title must be at most 160 characters")
        String title,

        @NotBlank(message = "Provider is required")
        @Size(max = 120, message = "Provider must be at most 120 characters")
        String provider,

        @Size(max = 500, message = "URL must be at most 500 characters")
        String url,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Status is required")
        CourseStatus status,

        @NotNull(message = "Total hours is required")
        @Min(value = 0, message = "Total hours cannot be negative")
        @Max(value = 10000, message = "Total hours must be at most 10000")
        Integer totalHours,

        @NotNull(message = "Completed hours is required")
        @Min(value = 0, message = "Completed hours cannot be negative")
        @Max(value = 10000, message = "Completed hours must be at most 10000")
        Integer completedHours,

        LocalDate startedOn,

        LocalDate targetCompletionDate
) {
}
