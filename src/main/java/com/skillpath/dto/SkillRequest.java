package com.skillpath.dto;

import com.skillpath.domain.SkillLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload used to create or replace a {@link com.skillpath.domain.Skill}.
 */
public record SkillRequest(

        @NotBlank(message = "Skill name is required")
        @Size(max = 120, message = "Skill name must be at most 120 characters")
        String name,

        @NotBlank(message = "Category is required")
        @Size(max = 80, message = "Category must be at most 80 characters")
        String category,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Current level is required")
        SkillLevel currentLevel,

        @NotNull(message = "Target level is required")
        SkillLevel targetLevel
) {
}
