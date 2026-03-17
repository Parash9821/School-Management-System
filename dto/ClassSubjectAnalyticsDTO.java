package com.school.School_Management_System.dto;

public class ClassSubjectAnalyticsDTO {

    private Long subjectId;
    private String subjectName;

    private int studentsCount;

    private double averagePercent;
    private double highestPercent;
    private double lowestPercent;

    public ClassSubjectAnalyticsDTO(Long subjectId, String subjectName, int studentsCount,
                                    double averagePercent, double highestPercent, double lowestPercent) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.studentsCount = studentsCount;
        this.averagePercent = averagePercent;
        this.highestPercent = highestPercent;
        this.lowestPercent = lowestPercent;
    }

    public Long getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public int getStudentsCount() { return studentsCount; }
    public double getAveragePercent() { return averagePercent; }
    public double getHighestPercent() { return highestPercent; }
    public double getLowestPercent() { return lowestPercent; }
}
