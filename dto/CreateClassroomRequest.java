package com.school.School_Management_System.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateClassroomRequest {
    @NotBlank
    public String name;   // e.g. "Grade 10 A"
}
