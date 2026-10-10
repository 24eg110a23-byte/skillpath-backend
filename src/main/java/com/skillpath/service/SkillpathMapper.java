package com.skillpath.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.skillpath.domain.Course;
import com.skillpath.domain.Skill;
import com.skillpath.domain.SkillCourseLink;
import com.skillpath.dto.CourseResponse;
import com.skillpath.dto.LinkedCourseResponse;
import com.skillpath.dto.LinkedSkillResponse;
import com.skillpath.dto.SkillResponse;
import com.skillpath.repository.SkillCourseLinkRepository;

/**
 * Converts domain entities into the read models the API exposes.
 *
 * <p>Mapping lives here rather than in the entities so persistence stays free of
 * transport concerns, and rather than in the controllers so several services can
 * reuse the exact same shape.</p>
 */
@Component
public class SkillpathMapper {

    private final SkillCourseLinkRepository linkRepository;

    public SkillpathMapper(SkillCourseLinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    /** Number of linked courses, keyed by skill id. One query for the whole page. */
    public Map<Long, Integer> courseCountsBySkill() {
        Map<Long, Integer> counts = new HashMap<>();
        for (SkillCourseLinkRepository.OwnerCount row : linkRepository.countGroupedBySkill()) {
            counts.put(row.getOwnerId(), (int) row.getTotal());
        }
        return counts;
    }

    /** Number of linked skills, keyed by course id. One query for the whole page. */
    public Map<Long, Integer> skillCountsByCourse() {
        Map<Long, Integer> counts = new HashMap<>();
        for (SkillCourseLinkRepository.OwnerCount row : linkRepository.countGroupedByCourse()) {
            counts.put(row.getOwnerId(), (int) row.getTotal());
        }
        return counts;
    }

    public SkillResponse toSkill(Skill skill, int courseCount) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCategory(),
                skill.getDescription(),
                skill.getCurrentLevel(),
                skill.getCurrentLevel().getLabel(),
                skill.getTargetLevel(),
                skill.getTargetLevel().getLabel(),
                skill.getProgressPercent(),
                skill.getStatus(),
                courseCount,
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }

    public List<SkillResponse> toSkills(List<Skill> skills, Map<Long, Integer> courseCounts) {
        return skills.stream()
                .map(skill -> toSkill(skill, courseCounts.getOrDefault(skill.getId(), 0)))
                .toList();
    }

    public CourseResponse toCourse(Course course, int skillCount) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getProvider(),
                course.getUrl(),
                course.getDescription(),
                course.getStatus(),
                course.getStatus().getLabel(),
                course.getTotalHours(),
                course.getCompletedHours(),
                course.getProgressPercent(),
                course.getStartedOn(),
                course.getTargetCompletionDate(),
                skillCount,
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }

    public List<CourseResponse> toCourses(List<Course> courses, Map<Long, Integer> skillCounts) {
        return courses.stream()
                .map(course -> toCourse(course, skillCounts.getOrDefault(course.getId(), 0)))
                .toList();
    }

    public LinkedCourseResponse toLinkedCourse(SkillCourseLink link, Map<Long, Integer> skillCountsByCourse) {
        Course course = link.getCourse();
        return new LinkedCourseResponse(
                link.getId(),
                link.getRelevance(),
                link.getRelevance().getLabel(),
                link.getNotes(),
                link.getCreatedAt(),
                toCourse(course, skillCountsByCourse.getOrDefault(course.getId(), 0))
        );
    }

    public LinkedSkillResponse toLinkedSkill(SkillCourseLink link, Map<Long, Integer> courseCountsBySkill) {
        Skill skill = link.getSkill();
        return new LinkedSkillResponse(
                link.getId(),
                link.getRelevance(),
                link.getRelevance().getLabel(),
                link.getNotes(),
                link.getCreatedAt(),
                toSkill(skill, courseCountsBySkill.getOrDefault(skill.getId(), 0))
        );
    }
}
