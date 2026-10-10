package com.skillpath.dto;

import java.util.List;

/**
 * Aggregated snapshot backing the dashboard screen.
 */
public record DashboardResponse(
        SkillTotals skills,
        CourseTotals courses,
        List<GroupCount> skillsByLevel,
        List<GroupCount> skillsByCategory,
        List<GroupCount> coursesByStatus,
        List<SkillResponse> focusSkills,
        List<SkillResponse> nearestTargets,
        List<CourseResponse> activeCourses,
        int totalLinks,
        int averageSkillProgress,
        int averageCourseProgress
) {

    /**
     * Counts describing the skill portfolio.
     */
    public record SkillTotals(
            int total,
            int achieved,
            int inProgress,
            int notStarted
    ) {
    }

    /**
     * Counts describing the course library.
     */
    public record CourseTotals(
            int total,
            int completed,
            int inProgress,
            int notStarted,
            int totalHours,
            int completedHours
    ) {
    }
}
