package com.skillpath.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A single capability the learner is building, together with where they are now
 * and where they want to get to.
 */
@Entity
@Table(
        name = "skills",
        uniqueConstraints = @UniqueConstraint(name = "uk_skill_name", columnNames = "name")
)
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "category", nullable = false, length = 80)
    private String category;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false, length = 20)
    private SkillLevel currentLevel = SkillLevel.BEGINNER;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_level", nullable = false, length = 20)
    private SkillLevel targetLevel = SkillLevel.INTERMEDIATE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "skill",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SkillCourseLink> courseLinks = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /** Adds a link and keeps both sides of the relationship consistent. */
    public void addCourseLink(SkillCourseLink link) {
        courseLinks.add(link);
        link.setSkill(this);
    }

    public void removeCourseLink(SkillCourseLink link) {
        courseLinks.remove(link);
        link.setSkill(null);
    }

    public int getProgressPercent() {
        return SkillLevel.progressPercent(currentLevel, targetLevel);
    }

    public SkillStatus getStatus() {
        return SkillStatus.from(currentLevel, targetLevel);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SkillLevel getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(SkillLevel currentLevel) {
        this.currentLevel = currentLevel;
    }

    public SkillLevel getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(SkillLevel targetLevel) {
        this.targetLevel = targetLevel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<SkillCourseLink> getCourseLinks() {
        return courseLinks;
    }
}
