package com.school.School_Management_System.dto;

public class StudentSubjectResultDTO {

    private Long subjectId;
    private String subjectName;
    private int obtained;
    private int max;
    private double percentage;
    private String grade;
    private boolean pass;

    public StudentSubjectResultDTO(Long subjectId, String subjectName,
                                   int obtained, int max,
                                   double percentage, String grade, boolean pass) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.obtained = obtained;
        this.max = max;
        this.percentage = percentage;
        this.grade = grade;
        this.pass = pass;
    }

    public Long getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public int getObtained() { return obtained; }
    public int getMax() { return max; }
    public double getPercentage() { return percentage; }
    public String getGrade() { return grade; }
    public boolean isPass() { return pass; }
}
