package com.skillpath.dto;

import java.time.Instant;

import com.skillpath.domain.SkillLevel;
import com.skillpath.domain.SkillStatus;

/**
 * Read model for a skill. Everything is derived on the server so the client
 * never has to re-implement the progress rules.
 */
public record SkillResponse(
        Long id,
        String name,
        String category,
        String description,
        SkillLevel currentLevel,
        String currentLevelLabel,
        SkillLevel targetLevel,
        String targetLevelLabel,
        int progressPercent,
        SkillStatus status,
        int courseCount,
        Instant createdAt,
        Instant updatedAt
) {
}
