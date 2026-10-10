package com.skillpath.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillpath.domain.Course;
import com.skillpath.domain.Relevance;
import com.skillpath.domain.Skill;
import com.skillpath.domain.SkillCourseLink;
import com.skillpath.domain.SkillLevel;
import com.skillpath.dto.LinkedCourseResponse;
import com.skillpath.dto.PageResponse;
import com.skillpath.dto.SkillDetailResponse;
import com.skillpath.dto.SkillRequest;
import com.skillpath.dto.SkillResponse;
import com.skillpath.dto.UpdateSkillLevelRequest;
import com.skillpath.error.DuplicateResourceException;
import com.skillpath.error.ResourceNotFoundException;
import com.skillpath.repository.CourseRepository;
import com.skillpath.repository.SkillCourseLinkRepository;
import com.skillpath.repository.SkillRepository;

/**
 * Business rules for skills and the skill-to-course relationships.
 */
@Service
@Transactional
public class SkillService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    /** Columns callers are allowed to sort by, mapped to entity property names. */
    private static final Map<String, String> SORTABLE = Map.of(
            "name", "name",
            "category", "category",
            "currentLevel", "currentLevel",
            "targetLevel", "targetLevel",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt"
    );

    private final SkillRepository skillRepository;
    private final CourseRepository courseRepository;
    private final SkillCourseLinkRepository linkRepository;
    private final SkillpathMapper mapper;

    public SkillService(SkillRepository skillRepository,
                        CourseRepository courseRepository,
                        SkillCourseLinkRepository linkRepository,
                        SkillpathMapper mapper) {
        this.skillRepository = skillRepository;
        this.courseRepository = courseRepository;
        this.linkRepository = linkRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<SkillResponse> listSkills(String search, String category, int page, int size,
                                                  String sortBy, String direction) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                clampSize(size),
                resolveSort(sortBy, direction)
        );
        Page<Skill> result = skillRepository.search(blankToNull(search), blankToNull(category), pageRequest);
        Map<Long, Integer> counts = mapper.courseCountsBySkill();
        return PageResponse.from(result, skill -> mapper.toSkill(skill, counts.getOrDefault(skill.getId(), 0)));
    }

    @Transactional(readOnly = true)
    public SkillResponse getSkill(Long id) {
        Skill skill = requireSkill(id);
        return mapper.toSkill(skill, (int) linkRepository.countBySkillId(id));
    }

    @Transactional(readOnly = true)
    public SkillDetailResponse getSkillDetail(Long id) {
        Skill skill = requireSkill(id);
        Map<Long, Integer> skillCounts = mapper.skillCountsByCourse();
        List<LinkedCourseResponse> courses = linkRepository.findAllBySkillId(id).stream()
                .map(link -> mapper.toLinkedCourse(link, skillCounts))
                .sorted((left, right) -> left.course().title().compareToIgnoreCase(right.course().title()))
                .toList();
        return new SkillDetailResponse(mapper.toSkill(skill, courses.size()), courses);
    }

    public SkillResponse createSkill(SkillRequest request) {
        String name = request.name().trim();
        if (skillRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A skill named '" + name + "' already exists");
        }
        validateLevels(request.currentLevel(), request.targetLevel());
        Skill skill = new Skill();
        apply(skill, request);
        return mapper.toSkill(skillRepository.save(skill), 0);
    }

    public SkillResponse updateSkill(Long id, SkillRequest request) {
        Skill skill = requireSkill(id);
        String name = request.name().trim();
        skillRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("A skill named '" + name + "' already exists");
                });
        validateLevels(request.currentLevel(), request.targetLevel());
        apply(skill, request);
        return mapper.toSkill(skillRepository.save(skill), (int) linkRepository.countBySkillId(id));
    }

    public SkillResponse updateSkillLevel(Long id, UpdateSkillLevelRequest request) {
        Skill skill = requireSkill(id);
        validateLevels(request.currentLevel(), skill.getTargetLevel());
        skill.setCurrentLevel(request.currentLevel());
        return mapper.toSkill(skillRepository.save(skill), (int) linkRepository.countBySkillId(id));
    }

    public void deleteSkill(Long id) {
        skillRepository.delete(requireSkill(id));
    }

    public LinkedCourseResponse linkCourse(Long skillId, Long courseId, Relevance relevance, String notes) {
        Skill skill = requireSkill(skillId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Course", courseId));
        if (linkRepository.existsBySkillIdAndCourseId(skillId, courseId)) {
            throw new DuplicateResourceException(
                    "Course '" + course.getTitle() + "' is already linked to skill '" + skill.getName() + "'");
        }
        Relevance effective = relevance == null ? Relevance.HELPFUL : relevance;
        SkillCourseLink link = new SkillCourseLink(skill, course, effective, trimToNull(notes));
        skill.addCourseLink(link);
        SkillCourseLink saved = linkRepository.save(link);
        return mapper.toLinkedCourse(saved, mapper.skillCountsByCourse());
    }

    public void unlinkCourse(Long skillId, Long courseId) {
        SkillCourseLink link = linkRepository.findBySkillIdAndCourseId(skillId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill " + skillId + " is not linked to course " + courseId));
        Skill parent = link.getSkill();
        if (parent != null) {
            parent.removeCourseLink(link);
        }
        linkRepository.delete(link);
    }

    @Transactional(readOnly = true)
    public List<String> listCategories() {
        return skillRepository.findDistinctCategories();
    }

    private void validateLevels(SkillLevel current, SkillLevel target) {
        if (target.getRank() < current.getRank()) {
            throw new IllegalArgumentException(
                    "Target level (" + target.getLabel() + ") cannot be lower than the current level ("
                            + current.getLabel() + ")");
        }
    }

    private Skill requireSkill(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Skill", id));
    }

    private void apply(Skill skill, SkillRequest request) {
        skill.setName(request.name().trim());
        skill.setCategory(request.category().trim());
        skill.setDescription(trimToNull(request.description()));
        skill.setCurrentLevel(request.currentLevel());
        skill.setTargetLevel(request.targetLevel());
    }

    private Sort resolveSort(String sortBy, String direction) {
        String property = SORTABLE.get(sortBy == null ? "" : sortBy);
        if (property == null) {
            property = "name";
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
