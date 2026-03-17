package com.school.School_Management_System.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class PublishResultRequest {

    @NotNull
    private Long classroomId;

    private String academicYear;

    private LocalDateTime publishAt; // ✅ change from String to LocalDateTime

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDateTime getPublishAt() { return publishAt; }
    public void setPublishAt(LocalDateTime publishAt) { this.publishAt = publishAt; }
}
