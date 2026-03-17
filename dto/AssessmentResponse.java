package com.school.School_Management_System.dto;

import java.time.LocalDate;

public class AssessmentResponse {
    private Long id;
    private Long classroomId;
    private Long subjectId;
    private String type;
    private int maxMarks;
    private LocalDate heldOn;

    public AssessmentResponse() {}

    public AssessmentResponse(Long id, Long classroomId, Long subjectId, String type, int maxMarks, LocalDate heldOn) {
        this.id = id;
        this.classroomId = classroomId;
        this.subjectId = subjectId;
        this.type = type;
        this.maxMarks = maxMarks;
        this.heldOn = heldOn;
    }

    public Long getId() { return id; }
    public Long getClassroomId() { return classroomId; }
    public Long getSubjectId() { return subjectId; }
    public String getType() { return type; }
    public int getMaxMarks() { return maxMarks; }
    public LocalDate getHeldOn() { return heldOn; }

    public void setId(Long id) { this.id = id; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public void setType(String type) { this.type = type; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }
    public void setHeldOn(LocalDate heldOn) { this.heldOn = heldOn; }
}
