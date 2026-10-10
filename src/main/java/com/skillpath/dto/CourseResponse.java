package com.skillpath.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.skillpath.domain.CourseStatus;

/**
 * Read model for a course.
 */
public record CourseResponse(
        Long id,
        String title,
        String provider,
        String url,
        String description,
        CourseStatus status,
        String statusLabel,
        Integer totalHours,
        Integer completedHours,
        int progressPercent,
        LocalDate startedOn,
        LocalDate targetCompletionDate,
        int skillCount,
        Instant createdAt,
        Instant updatedAt
) {
}
