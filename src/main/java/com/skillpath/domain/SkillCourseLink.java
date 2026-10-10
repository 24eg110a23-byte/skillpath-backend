package com.skillpath.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Join entity between {@link Skill} and {@link Course}.
 *
 * <p>Modelled as an entity rather than a plain {@code @ManyToMany} so the
 * relationship can carry its own attributes (relevance, notes) and so the
 * schema stays easy to migrate to MySQL or PostgreSQL.</p>
 */
@Entity
@Table(
        name = "skill_course_links",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_skill_course",
                columnNames = {"skill_id", "course_id"}
        )
)
public class SkillCourseLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(name = "relevance", nullable = false, length = 20)
    private Relevance relevance = Relevance.HELPFUL;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SkillCourseLink() {
        // required by JPA
    }

    public SkillCourseLink(Skill skill, Course course, Relevance relevance, String notes) {
        this.skill = skill;
        this.course = course;
        this.relevance = relevance;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Relevance getRelevance() {
        return relevance;
    }

    public void setRelevance(Relevance relevance) {
        this.relevance = relevance;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
