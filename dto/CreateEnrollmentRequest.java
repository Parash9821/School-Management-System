package com.school.School_Management_System.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class CreateEnrollmentRequest {
    @NotNull
    public Long studentId;

    @NotNull
    public Long classroomId;

    @NotBlank
    public String academicYear; // "2025-2026"
}
