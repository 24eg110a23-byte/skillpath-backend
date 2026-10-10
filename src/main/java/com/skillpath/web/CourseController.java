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

import com.skillpath.domain.CourseStatus;
import com.skillpath.dto.CourseDetailResponse;
import com.skillpath.dto.CourseRequest;
import com.skillpath.dto.CourseResponse;
import com.skillpath.dto.PageResponse;
import com.skillpath.dto.UpdateCourseProgressRequest;
import com.skillpath.service.CourseService;

import jakarta.validation.Valid;

/**
 * REST surface for the course library.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public PageResponse<CourseResponse> listCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return courseService.listCourses(search, status, page, size, sortBy, direction);
    }

    @GetMapping("/providers")
    public List<String> listProviders() {
        return courseService.listProviders();
    }

    @GetMapping("/{id}")
    public CourseResponse getCourse(@PathVariable Long id) {
        return courseService.getCourse(id);
    }

    @GetMapping("/{id}/detail")
    public CourseDetailResponse getCourseDetail(@PathVariable Long id) {
        return courseService.getCourseDetail(id);
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse created = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public CourseResponse updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return courseService.updateCourse(id, request);
    }

    @PatchMapping("/{id}/progress")
    public CourseResponse updateProgress(@PathVariable Long id,
                                         @Valid @RequestBody UpdateCourseProgressRequest request) {
        return courseService.updateProgress(id, request.status(), request.completedHours());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
