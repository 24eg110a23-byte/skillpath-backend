package com.skillpath.dto;

import java.util.List;

/**
 * A skill together with the courses attached to it.
 */
public record SkillDetailResponse(
        SkillResponse skill,
        List<LinkedCourseResponse> courses
) {
}
