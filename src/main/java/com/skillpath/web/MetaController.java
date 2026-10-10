package com.skillpath.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillpath.domain.CourseStatus;
import com.skillpath.domain.Relevance;
import com.skillpath.domain.SkillLevel;
import com.skillpath.domain.SkillStatus;
import com.skillpath.dto.GroupCount;

/**
 * Exposes the option lists the UI needs (levels, statuses, relevance) together
 * with their display labels, so the frontend never hard-codes a duplicate mapping.
 */
@RestController
@RequestMapping("/api/meta")
public class MetaController {

    @GetMapping("/levels")
    public List<GroupCount> levels() {
        return Arrays.stream(SkillLevel.values())
                .map(level -> new GroupCount(level.name(), level.getLabel(), level.getRank()))
                .toList();
    }

    @GetMapping("/skill-statuses")
    public List<GroupCount> skillStatuses() {
        return Arrays.stream(SkillStatus.values())
                .map(status -> new GroupCount(status.name(), humanise(status.name()), 0))
                .toList();
    }

    @GetMapping("/course-statuses")
    public List<GroupCount> courseStatuses() {
        return Arrays.stream(CourseStatus.values())
                .map(status -> new GroupCount(status.name(), status.getLabel(), 0))
                .toList();
    }

    @GetMapping("/relevance")
    public List<GroupCount> relevance() {
        return Arrays.stream(Relevance.values())
                .map(value -> new GroupCount(value.name(), value.getLabel(), 0))
                .toList();
    }

    private String humanise(String enumName) {
        String lower = enumName.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
