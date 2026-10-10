package com.skillpath.dto;

import com.skillpath.domain.SkillLevel;

import jakarta.validation.constraints.NotNull;

/**
 * Payload for a focused update of a skill's current level.
 */
public record UpdateSkillLevelRequest(

        @NotNull(message = "Current level is required")
        SkillLevel currentLevel
) {
}
