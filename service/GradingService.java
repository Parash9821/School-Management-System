package com.school.School_Management_System.service;

import org.springframework.stereotype.Service;

@Service
public class GradingService {

    public String gradeFromPercentage(double percent) {
        if (percent >= 90) return "A+";
        if (percent >= 80) return "A";
        if (percent >= 70) return "B";
        if (percent >= 60) return "C";
        if (percent >= 50) return "D";
        return "F";
    }

    public boolean isPass(double percent) {
        return percent >= 50;
    }
}
