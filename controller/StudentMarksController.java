package com.school.School_Management_System.controller;

import com.school.School_Management_System.dto.PublishResultRequest;
import com.school.School_Management_System.model.Mark;
import com.school.School_Management_System.model.User;
import com.school.School_Management_System.repository.MarkRepository;
import com.school.School_Management_System.repository.UserRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.boot.availability.AvailabilityChangeEvent.publish;

@RestController
@RequestMapping("/api/student")
public class StudentMarksController {

    private final UserRepository userRepo;
    private final MarkRepository markRepo;

    public StudentMarksController(UserRepository userRepo, MarkRepository markRepo) {
        this.userRepo = userRepo;
        this.markRepo = markRepo;
    }

    @GetMapping("/marks")
    public List<StudentMarkResponse> myMarks(Authentication auth) {
        User me = userRepo.findByUsername(auth.getName()).orElseThrow();

        return markRepo.findByStudent_Id(me.getId())
                .stream()
                .map(StudentMarkResponse::from)
                .toList();
    }

    // DTO (prevents Hibernate proxy serialization problems)
    public static class StudentMarkResponse {
        private Long markId;
        private Long assessmentId;
        private String assessmentType;
        private LocalDate heldOn;

        private Long subjectId;
        private String subjectName;

        private int marks;
        private int maxMarks;
        private String feedback;

        public static StudentMarkResponse from(Mark m) {
            StudentMarkResponse r = new StudentMarkResponse();
            r.markId = m.getId();

            if (m.getAssessment() != null) {
                r.assessmentId = m.getAssessment().getId();
                r.assessmentType = m.getAssessment().getType();
                r.heldOn = m.getAssessment().getHeldOn();
                r.maxMarks = m.getAssessment().getMaxMarks();

                if (m.getAssessment().getSubject() != null) {
                    r.subjectId = m.getAssessment().getSubject().getId();
                    r.subjectName = m.getAssessment().getSubject().getName();
                }
            }

            r.marks = m.getMarks();
            r.feedback = m.getFeedback();
            return r;
        }

        public Long getMarkId() { return markId; }
        public Long getAssessmentId() { return assessmentId; }
        public String getAssessmentType() { return assessmentType; }
        public LocalDate getHeldOn() { return heldOn; }
        public Long getSubjectId() { return subjectId; }
        public String getSubjectName() { return subjectName; }
        public int getMarks() { return marks; }
        public int getMaxMarks() { return maxMarks; }
        public String getFeedback() { return feedback; }
    }

}
