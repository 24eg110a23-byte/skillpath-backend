package com.skillpath.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

/**
 * A learning resource the user can attach to one or more {@link Skill}s.
 */
@Entity
@Table(
        name = "courses",
        uniqueConstraints = @UniqueConstraint(name = "uk_course_title", columnNames = "title")
)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "provider", nullable = false, length = 120)
    private String provider;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CourseStatus status = CourseStatus.NOT_STARTED;

    @Column(name = "total_hours", nullable = false)
    private Integer totalHours = 0;

    @Column(name = "completed_hours", nullable = false)
    private Integer completedHours = 0;

    @Column(name = "started_on")
    private LocalDate startedOn;

    @Column(name = "target_completion_date")
    private LocalDate targetCompletionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SkillCourseLink> skillLinks = new ArrayList<>();

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

    public int getProgressPercent() {
        if (status == CourseStatus.COMPLETED) {
            return 100;
        }
        if (totalHours == null || totalHours <= 0) {
            return 0;
        }
        int raw = Math.round(((completedHours == null ? 0 : completedHours) * 100f) / totalHours);
        return Math.min(100, Math.max(0, raw));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public Integer getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Integer totalHours) {
        this.totalHours = totalHours;
    }

    public Integer getCompletedHours() {
        return completedHours;
    }

    public void setCompletedHours(Integer completedHours) {
        this.completedHours = completedHours;
    }

    public LocalDate getStartedOn() {
        return startedOn;
    }

    public void setStartedOn(LocalDate startedOn) {
        this.startedOn = startedOn;
    }

    public LocalDate getTargetCompletionDate() {
        return targetCompletionDate;
    }

    public void setTargetCompletionDate(LocalDate targetCompletionDate) {
        this.targetCompletionDate = targetCompletionDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<SkillCourseLink> getSkillLinks() {
        return skillLinks;
    }
}
