package com.school.School_Management_System.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "result_publish",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_result_publish_school_classroom_year",
                columnNames = {"school_id", "classroom_id", "academic_year"}
        )
)
public class ResultPublish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(optional = false)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(name = "academic_year", nullable = false, length = 9)
    private String academicYear;

    @Column(name = "published", nullable = false)
    private boolean published = false;

    // Optional schedule time (if not null -> scheduled)
    @Column(name = "publish_at")
    private LocalDateTime publishAt;

    // When actually published
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // Who performed last action (publish/unpublish/schedule)
    @ManyToOne(optional = false)
    @JoinColumn(name = "published_by_user_id", nullable = false)
    private User publishedBy;

    // ===== Getters/Setters =====

    public Long getId() { return id; }

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }

    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public LocalDateTime getPublishAt() { return publishAt; }
    public void setPublishAt(LocalDateTime publishAt) { this.publishAt = publishAt; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public User getPublishedBy() { return publishedBy; }
    public void setPublishedBy(User publishedBy) { this.publishedBy = publishedBy; }
}
