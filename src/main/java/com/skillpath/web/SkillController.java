package com.skillpath.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillpath.dto.LinkCourseRequest;
import com.skillpath.dto.LinkedCourseResponse;
import com.skillpath.dto.PageResponse;
import com.skillpath.dto.SkillDetailResponse;
import com.skillpath.dto.SkillRequest;
import com.skillpath.dto.SkillResponse;
import com.skillpath.dto.UpdateSkillLevelRequest;
import com.skillpath.service.SkillService;

import jakarta.validation.Valid;

/**
 * REST surface for skills and their linked courses.
 */
@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public PageResponse<SkillResponse> listSkills(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return skillService.listSkills(search, category, page, size, sortBy, direction);
    }

    @GetMapping("/categories")
    public List<String> listCategories() {
        return skillService.listCategories();
    }

    @GetMapping("/{id}")
    public SkillResponse getSkill(@PathVariable Long id) {
        return skillService.getSkill(id);
    }

    @GetMapping("/{id}/detail")
    public SkillDetailResponse getSkillDetail(@PathVariable Long id) {
        return skillService.getSkillDetail(id);
    }

    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(@Valid @RequestBody SkillRequest request) {
        SkillResponse created = skillService.createSkill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public SkillResponse updateSkill(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        return skillService.updateSkill(id, request);
    }

    @PatchMapping("/{id}/level")
    public SkillResponse updateSkillLevel(@PathVariable Long id,
                                          @Valid @RequestBody UpdateSkillLevelRequest request) {
        return skillService.updateSkillLevel(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/courses")
    public ResponseEntity<LinkedCourseResponse> linkCourse(@PathVariable Long id,
                                                           @Valid @RequestBody LinkCourseRequest request) {
        LinkedCourseResponse linked = skillService.linkCourse(
                id, request.courseId(), request.relevance(), request.notes());
        return ResponseEntity.status(HttpStatus.CREATED).body(linked);
    }

    @DeleteMapping("/{id}/courses/{courseId}")
    public ResponseEntity<Void> unlinkCourse(@PathVariable Long id, @PathVariable Long courseId) {
        skillService.unlinkCourse(id, courseId);
        return ResponseEntity.noContent().build();
    }
}
