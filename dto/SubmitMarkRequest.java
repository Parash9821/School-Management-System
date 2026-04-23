package com.school.School_Management_System.dto;

import jakarta.validation.constraints.NotNull;

public class SubmitMarkRequest {

    @NotNull
    private Long assessmentId;

    @NotNull
    private Long studentId;

    @NotNull
    private Integer marks;

    private String feedback;

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
