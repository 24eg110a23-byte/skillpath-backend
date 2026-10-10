package com.skillpath.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillpath.domain.Course;
import com.skillpath.domain.CourseStatus;
import com.skillpath.domain.Skill;
import com.skillpath.domain.SkillLevel;
import com.skillpath.domain.SkillStatus;
import com.skillpath.dto.CourseResponse;
import com.skillpath.dto.DashboardResponse;
import com.skillpath.dto.GroupCount;
import com.skillpath.dto.SkillResponse;
import com.skillpath.repository.CourseRepository;
import com.skillpath.repository.SkillCourseLinkRepository;
import com.skillpath.repository.SkillRepository;

/**
 * Builds the dashboard snapshot.
 *
 * <p>The portfolio is small enough (tens of rows) that aggregating in memory keeps the
 * code readable and the number of SQL round trips at a constant three.</p>
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int SPOTLIGHT_LIMIT = 5;

    private final SkillRepository skillRepository;
    private final CourseRepository courseRepository;
    private final SkillCourseLinkRepository linkRepository;
    private final SkillpathMapper mapper;

    public DashboardService(SkillRepository skillRepository,
                            CourseRepository courseRepository,
                            SkillCourseLinkRepository linkRepository,
                            SkillpathMapper mapper) {
        this.skillRepository = skillRepository;
        this.courseRepository = courseRepository;
        this.linkRepository = linkRepository;
        this.mapper = mapper;
    }

    public DashboardResponse getDashboard() {
        List<Skill> skills = skillRepository.findAllByOrderByNameAsc();
        List<Course> courses = courseRepository.findAllByOrderByTitleAsc();
        Map<Long, Integer> courseCounts = mapper.courseCountsBySkill();
        Map<Long, Integer> skillCounts = mapper.skillCountsByCourse();

        List<SkillResponse> skillResponses = mapper.toSkills(skills, courseCounts);
        List<CourseResponse> courseResponses = mapper.toCourses(courses, skillCounts);

        return new DashboardResponse(
                buildSkillTotals(skillResponses),
                buildCourseTotals(courseResponses),
                buildSkillsByLevel(skills),
                buildSkillsByCategory(skills),
                buildCoursesByStatus(courses),
                buildFocusSkills(skillResponses),
                buildNearestTargets(skillResponses),
                buildActiveCourses(courseResponses),
                (int) linkRepository.count(),
                average(skillResponses.stream().mapToInt(SkillResponse::progressPercent).toArray()),
                average(courseResponses.stream().mapToInt(CourseResponse::progressPercent).toArray())
        );
    }

    private DashboardResponse.SkillTotals buildSkillTotals(List<SkillResponse> skills) {
        int total = skills.size();
        int achieved = count(skills, SkillStatus.ACHIEVED);
        int inProgress = count(skills, SkillStatus.IN_PROGRESS);
        int notStarted = count(skills, SkillStatus.NOT_STARTED);
        return new DashboardResponse.SkillTotals(total, achieved, inProgress, notStarted);
    }

    private DashboardResponse.CourseTotals buildCourseTotals(List<CourseResponse> courses) {
        int total = courses.size();
        int completed = countCourses(courses, CourseStatus.COMPLETED);
        int inProgress = countCourses(courses, CourseStatus.IN_PROGRESS);
        int notStarted = countCourses(courses, CourseStatus.NOT_STARTED);
        int totalHours = courses.stream().mapToInt(course -> valueOrZero(course.totalHours())).sum();
        int completedHours = courses.stream().mapToInt(course -> valueOrZero(course.completedHours())).sum();
        return new DashboardResponse.CourseTotals(
                total, completed, inProgress, notStarted, totalHours, completedHours);
    }

    /** Every rung of the ladder is reported, including the ones nobody occupies. */
    private List<GroupCount> buildSkillsByLevel(List<Skill> skills) {
        Map<SkillLevel, Long> tally = new TreeMap<>(Comparator.comparingInt(SkillLevel::getRank));
        for (SkillLevel level : SkillLevel.values()) {
            tally.put(level, 0L);
        }
        for (Skill skill : skills) {
            tally.merge(skill.getTargetLevel(), 1L, Long::sum);
        }
        return tally.entrySet().stream()
                .map(entry -> new GroupCount(entry.getKey().name(), entry.getKey().getLabel(), entry.getValue()))
                .toList();
    }

    private List<GroupCount> buildSkillsByCategory(List<Skill> skills) {
        Map<String, Long> tally = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Skill skill : skills) {
            tally.merge(skill.getCategory(), 1L, Long::sum);
        }
        return tally.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER)))
                .map(entry -> new GroupCount(entry.getKey(), entry.getKey(), entry.getValue()))
                .toList();
    }

    /** Every course status is reported so the chart always has three slices. */
    private List<GroupCount> buildCoursesByStatus(List<Course> courses) {
        Map<CourseStatus, Long> tally = new LinkedHashMap<>();
        for (CourseStatus status : CourseStatus.values()) {
            tally.put(status, 0L);
        }
        for (Course course : courses) {
            tally.merge(course.getStatus(), 1L, Long::sum);
        }
        return tally.entrySet().stream()
                .map(entry -> new GroupCount(entry.getKey().name(), entry.getKey().getLabel(), entry.getValue()))
                .toList();
    }

    private List<SkillResponse> buildFocusSkills(List<SkillResponse> skills) {
        List<SkillResponse> ordered = new ArrayList<>(skills.stream()
                .filter(skill -> skill.status() == SkillStatus.IN_PROGRESS)
                .toList());
        ordered.sort(Comparator.comparingInt(SkillResponse::progressPercent).reversed()
                .thenComparing(SkillResponse::name, String.CASE_INSENSITIVE_ORDER));
        return ordered.stream().limit(SPOTLIGHT_LIMIT).toList();
    }

    private List<SkillResponse> buildNearestTargets(List<SkillResponse> skills) {
        List<SkillResponse> ordered = new ArrayList<>(skills.stream()
                .filter(skill -> skill.status() != SkillStatus.ACHIEVED)
                .toList());
        ordered.sort(Comparator
                .comparingInt((SkillResponse skill) -> skill.targetLevel().getRank() - skill.currentLevel().getRank())
                .thenComparing(Comparator.comparingInt(SkillResponse::progressPercent).reversed()));
        return ordered.stream().limit(SPOTLIGHT_LIMIT).toList();
    }

    private List<CourseResponse> buildActiveCourses(List<CourseResponse> courses) {
        return courses.stream()
                .filter(course -> course.status() == CourseStatus.IN_PROGRESS)
                .sorted(Comparator.comparingInt(CourseResponse::progressPercent).reversed()
                        .thenComparing(CourseResponse::title, String.CASE_INSENSITIVE_ORDER))
                .limit(SPOTLIGHT_LIMIT)
                .toList();
    }

    private int count(List<SkillResponse> skills, SkillStatus status) {
        return (int) skills.stream().filter(skill -> skill.status() == status).count();
    }

    private int countCourses(List<CourseResponse> courses, CourseStatus status) {
        return (int) courses.stream().filter(course -> course.status() == status).count();
    }

    private int average(int[] values) {
        if (values.length == 0) {
            return 0;
        }
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return Math.round((float) sum / values.length);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
