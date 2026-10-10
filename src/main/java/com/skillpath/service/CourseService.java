package com.skillpath.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillpath.domain.Course;
import com.skillpath.domain.CourseStatus;
import com.skillpath.dto.CourseDetailResponse;
import com.skillpath.dto.CourseRequest;
import com.skillpath.dto.CourseResponse;
import com.skillpath.dto.LinkedSkillResponse;
import com.skillpath.dto.PageResponse;
import com.skillpath.error.DuplicateResourceException;
import com.skillpath.error.ResourceNotFoundException;
import com.skillpath.repository.CourseRepository;
import com.skillpath.repository.SkillCourseLinkRepository;

/**
 * Business rules for the course library, including the rules that keep
 * completion hours and course status consistent with each other.
 */
@Service
@Transactional
public class CourseService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    /** Columns callers are allowed to sort by, mapped to entity property names. */
    private static final Map<String, String> SORTABLE = Map.of(
            "title", "title",
            "provider", "provider",
            "status", "status",
            "totalHours", "totalHours",
            "completedHours", "completedHours",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt"
    );

    private final CourseRepository courseRepository;
    private final SkillCourseLinkRepository linkRepository;
    private final SkillpathMapper mapper;

    public CourseService(CourseRepository courseRepository,
                         SkillCourseLinkRepository linkRepository,
                         SkillpathMapper mapper) {
        this.courseRepository = courseRepository;
        this.linkRepository = linkRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> listCourses(String search, CourseStatus status, int page, int size,
                                                    String sortBy, String direction) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                clampSize(size),
                resolveSort(sortBy, direction)
        );
        Page<Course> result = courseRepository.search(blankToNull(search), status, pageRequest);
        Map<Long, Integer> counts = mapper.skillCountsByCourse();
        return PageResponse.from(result, course -> mapper.toCourse(course, counts.getOrDefault(course.getId(), 0)));
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long id) {
        Course course = requireCourse(id);
        return mapper.toCourse(course, (int) linkRepository.countByCourseId(id));
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetail(Long id) {
        Course course = requireCourse(id);
        Map<Long, Integer> skillCounts = mapper.courseCountsBySkill();
        List<LinkedSkillResponse> skills = linkRepository.findAllByCourseId(id).stream()
                .map(link -> mapper.toLinkedSkill(link, skillCounts))
                .sorted((left, right) -> left.skill().name().compareToIgnoreCase(right.skill().name()))
                .toList();
        return new CourseDetailResponse(mapper.toCourse(course, skills.size()), skills);
    }

    public CourseResponse createCourse(CourseRequest request) {
        String title = request.title().trim();
        if (courseRepository.existsByTitleIgnoreCase(title)) {
            throw new DuplicateResourceException("A course titled '" + title + "' already exists");
        }
        Course course = new Course();
        apply(course, request);
        return mapper.toCourse(courseRepository.save(course), 0);
    }

    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = requireCourse(id);
        String title = request.title().trim();
        courseRepository.findByTitleIgnoreCase(title)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("A course titled '" + title + "' already exists");
                });
        apply(course, request);
        return mapper.toCourse(courseRepository.save(course), (int) linkRepository.countByCourseId(id));
    }

    /** Focused update used by the progress slider on the course screen. */
    public CourseResponse updateProgress(Long id, CourseStatus status, Integer completedHours) {
        Course course = requireCourse(id);
        if (completedHours != null) {
            course.setCompletedHours(completedHours);
        }
        if (status != null) {
            course.setStatus(status);
        }
        normaliseProgress(course);
        return mapper.toCourse(courseRepository.save(course), (int) linkRepository.countByCourseId(id));
    }

    public void deleteCourse(Long id) {
        courseRepository.delete(requireCourse(id));
    }

    @Transactional(readOnly = true)
    public List<String> listProviders() {
        return courseRepository.findDistinctProviders();
    }

    private Course requireCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Course", id));
    }

    private void apply(Course course, CourseRequest request) {
        course.setTitle(request.title().trim());
        course.setProvider(request.provider().trim());
        course.setUrl(trimToNull(request.url()));
        course.setDescription(trimToNull(request.description()));
        course.setStatus(request.status());
        course.setTotalHours(request.totalHours());
        course.setCompletedHours(request.completedHours());
        course.setStartedOn(request.startedOn());
        course.setTargetCompletionDate(request.targetCompletionDate());
        validateDates(course);
        normaliseProgress(course);
    }

    private void validateDates(Course course) {
        LocalDate start = course.getStartedOn();
        LocalDate target = course.getTargetCompletionDate();
        if (start != null && target != null && target.isBefore(start)) {
            throw new IllegalArgumentException("Target completion date cannot be before the start date");
        }
    }

    /**
     * Keeps the three progress signals (status, total hours, completed hours) agreeing:
     * you cannot have finished more hours than the course offers, a completed course is
     * fully done, and an untouched course has no hours logged.
     */
    private void normaliseProgress(Course course) {
        int total = course.getTotalHours() == null ? 0 : course.getTotalHours();
        int completed = course.getCompletedHours() == null ? 0 : course.getCompletedHours();

        if (completed > total) {
            throw new IllegalArgumentException(
                    "Completed hours (" + completed + ") cannot exceed the total course hours (" + total + ")");
        }

        CourseStatus status = course.getStatus() == null ? CourseStatus.NOT_STARTED : course.getStatus();

        if (status == CourseStatus.COMPLETED) {
            completed = total;
        } else if (status == CourseStatus.NOT_STARTED) {
            completed = 0;
        } else if (total > 0 && completed >= total) {
            status = CourseStatus.COMPLETED;
        }

        course.setStatus(status);
        course.setCompletedHours(completed);
        course.setTotalHours(total);
    }

    private Sort resolveSort(String sortBy, String direction) {
        String property = SORTABLE.get(sortBy == null ? "" : sortBy);
        if (property == null) {
            property = "title";
        }
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, property);
    }

    private int clampSize(int size) {
        if (size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
