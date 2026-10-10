package com.skillpath.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.skillpath.domain.Skill;
import com.skillpath.domain.SkillLevel;
import com.skillpath.repository.CourseRepository;
import com.skillpath.repository.SkillRepository;

/**
 * Populates a freshly created database with starter skills so the dashboard has
 * something to show on first run.
 *
 * <p>Seeding is skipped whenever any skill or course already exists, which keeps the
 * app safe to restart against a persistent MySQL or PostgreSQL database.</p>
 */
@Component
@ConditionalOnProperty(name = "skillpath.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final SkillRepository skillRepository;
    private final CourseRepository courseRepository;

    public DataSeeder(SkillRepository skillRepository, CourseRepository courseRepository) {
        this.skillRepository = skillRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (skillRepository.count() > 0 || courseRepository.count() > 0) {
            log.info("Skillpath data already present - skipping seed");
            return;
        }

        Map<String, Skill> skills = seedSkills();
        log.info("Seeded Skillpath starter data: {} skills", skills.size());
    }

    private Map<String, Skill> seedSkills() {
        Map<String, Skill> skills = new LinkedHashMap<>();

        skills.put("React", skill("React", "Frontend",
                "Component thinking, hooks, render behaviour and state management.",
                SkillLevel.INTERMEDIATE, SkillLevel.ADVANCED));

        skills.put("TypeScript", skill("TypeScript", "Frontend",
                "Type-level modelling for larger JavaScript codebases.",
                SkillLevel.BEGINNER, SkillLevel.ADVANCED));

        skills.put("Spring Boot", skill("Spring Boot", "Backend",
                "Bootstrapping production services with Spring Boot 3 and JPA.",
                SkillLevel.INTERMEDIATE, SkillLevel.EXPERT));

        skills.put("Java", skill("Java", "Backend",
                "Core language, collections, generics and the modern language features.",
                SkillLevel.ADVANCED, SkillLevel.EXPERT));

        skills.put("SQL & Data Modelling", skill("SQL & Data Modelling", "Data",
                "Schema design, joins, indexing and query performance.",
                SkillLevel.INTERMEDIATE, SkillLevel.ADVANCED));

        skills.put("Docker", skill("Docker", "DevOps",
                "Containerising services and composing local environments.",
                SkillLevel.BEGINNER, SkillLevel.INTERMEDIATE));

        skills.put("Kubernetes", skill("Kubernetes", "DevOps",
                "Deployments, services, config and running workloads on a cluster.",
                SkillLevel.BEGINNER, SkillLevel.ADVANCED));

        skills.put("REST API Design", skill("REST API Design", "Backend",
                "Resource modelling, status codes, versioning and error contracts.",
                SkillLevel.ADVANCED, SkillLevel.ADVANCED));

        skills.put("Automated Testing", skill("Automated Testing", "Quality",
                "Unit, integration and end-to-end testing that actually catches regressions.",
                SkillLevel.BEGINNER, SkillLevel.INTERMEDIATE));

        skills.put("UI/UX Fundamentals", skill("UI/UX Fundamentals", "Design",
                "Hierarchy, spacing, contrast and interaction affordances.",
                SkillLevel.BEGINNER, SkillLevel.INTERMEDIATE));

        skills.put("Git & Version Control", skill("Git & Version Control", "Tooling",
                "Branching strategies, rebasing and readable history.",
                SkillLevel.ADVANCED, SkillLevel.ADVANCED));

        skills.put("System Design", skill("System Design", "Architecture",
                "Reasoning about scale, consistency, caching and failure modes.",
                SkillLevel.BEGINNER, SkillLevel.ADVANCED));

        skills.values().forEach(skillRepository::save);
        return skills;
    }

    private Skill skill(String name, String category, String description,
                        SkillLevel current, SkillLevel target) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setCategory(category);
        skill.setDescription(description);
        skill.setCurrentLevel(current);
        skill.setTargetLevel(target);
        return skill;
    }
}