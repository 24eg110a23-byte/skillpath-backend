package com.skillpath.dto;

import java.util.List;

/**
 * A course together with the skills it is attached to.
 */
public record CourseDetailResponse(
        CourseResponse course,
        List<LinkedSkillResponse> skills
) {
}
