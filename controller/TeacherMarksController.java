package com.school.School_Management_System.controller;

import com.school.School_Management_System.dto.CreateAssessmentRequest;
import com.school.School_Management_System.dto.SubmitMarkRequest;
import com.school.School_Management_System.model.*;
import com.school.School_Management_System.repository.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherMarksController {

    private final UserRepository userRepo;
    private final ClassroomRepository classroomRepo;
    private final SubjectRepository subjectRepo;
    private final TeachingAssignmentRepository assignmentRepo;
    private final AssessmentRepository assessmentRepo;
    private final MarkRepository markRepo;
    private final EnrollmentRepository enrollmentRepo;

    public TeacherMarksController(UserRepository userRepo,
                                  ClassroomRepository classroomRepo,
                                  SubjectRepository subjectRepo,
                                  TeachingAssignmentRepository assignmentRepo,
                                  AssessmentRepository assessmentRepo,
                                  MarkRepository markRepo,
                                  EnrollmentRepository enrollmentRepo) {
        this.userRepo = userRepo;
        this.classroomRepo = classroomRepo;
        this.subjectRepo = subjectRepo;
        this.assignmentRepo = assignmentRepo;
        this.assessmentRepo = assessmentRepo;
        this.markRepo = markRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    // ==========================
    // 1) Create Assessment
    // ==========================
    @PostMapping("/assessments")
    public AssessmentResponse createAssessment(@Valid @RequestBody CreateAssessmentRequest req,
                                               Authentication auth) {

        User teacher = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = teacher.getSchool().getId();

        Classroom classroom = classroomRepo.findById(req.getClassroomId()).orElseThrow();
        Subject subject = subjectRepo.findById(req.getSubjectId()).orElseThrow();

        TeachingAssignment ta = assignmentRepo
                .findBySchool_IdAndClassroom_IdAndSubject_Id(schoolId, classroom.getId(), subject.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No teaching assignment for this classroom & subject"));

        if (!ta.getTeacher().getId().equals(teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this subject");
        }

        Assessment a = new Assessment();
        a.setSchool(teacher.getSchool());
        a.setClassroom(classroom);
        a.setSubject(subject);
        a.setType(req.getType());
        a.setMaxMarks(req.getMaxMarks());
        a.setHeldOn(req.getHeldOn());
        a.setCreatedBy(teacher);

        return AssessmentResponse.from(assessmentRepo.save(a));
    }

    // ==========================
    // 2) List Assessments (for selection)
    // ==========================
    @GetMapping("/assessments")
    public List<AssessmentResponse> listAssessments(@RequestParam Long classroomId,
                                                    @RequestParam Long subjectId,
                                                    Authentication auth) {

        User teacher = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = teacher.getSchool().getId();

        // ensure teacher has assignment
        TeachingAssignment ta = assignmentRepo
                .findBySchool_IdAndClassroom_IdAndSubject_Id(schoolId, classroomId, subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No teaching assignment for this classroom & subject"));

        if (!ta.getTeacher().getId().equals(teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this subject");
        }


        return assessmentRepo
                .findByClassroom_IdAndSubject_Id(classroomId, subjectId)
                .stream()
                .map(AssessmentResponse::from)
                .toList();
    }

    // ==========================
    // 3) List Students by Classroom (NO academic year)


    public record StudentMini(Long id, String username, String fullName) {}

    // ==========================
    // 4) Submit / Update Mark (UPSERT)
    // ==========================
    @PostMapping("/marks")
    public MarkResponse submitMark(@Valid @RequestBody SubmitMarkRequest req, Authentication auth) {

        User teacher = userRepo.findByUsername(auth.getName()).orElseThrow();

        Long assessmentId = req.getAssessmentId();
        Long studentId = req.getStudentId();

        Assessment assessment = assessmentRepo.findById(assessmentId).orElseThrow();
        User student = userRepo.findById(studentId).orElseThrow();

        Mark m = markRepo.findByAssessment_IdAndStudent_Id(assessmentId, studentId)
                .orElseGet(Mark::new);

        m.setAssessment(assessment);
        m.setStudent(student);
        m.setMarks(req.getMarks());
        m.setFeedback(req.getFeedback());
        m.setEnteredBy(teacher);

        return MarkResponse.from(markRepo.save(m));
    }

    // ==========================
    // DTOs
    // ==========================
    public static class AssessmentResponse {
        private Long id;
        private Long classroomId;
        private Long subjectId;
        private String type;
        private int maxMarks;
        private LocalDate heldOn;

        public static AssessmentResponse from(Assessment a) {
            AssessmentResponse r = new AssessmentResponse();
            r.id = a.getId();
            r.classroomId = a.getClassroom() != null ? a.getClassroom().getId() : null;
            r.subjectId = a.getSubject() != null ? a.getSubject().getId() : null;
            r.type = a.getType();
            r.maxMarks = a.getMaxMarks();
            r.heldOn = a.getHeldOn();
            return r;
        }

        public Long getId() { return id; }
        public Long getClassroomId() { return classroomId; }
        public Long getSubjectId() { return subjectId; }
        public String getType() { return type; }
        public int getMaxMarks() { return maxMarks; }
        public LocalDate getHeldOn() { return heldOn; }
    }

    public static class MarkResponse {
        private Long id;
        private Long assessmentId;
        private Long studentId;
        private int marks;
        private String feedback;

        public static MarkResponse from(Mark m) {
            MarkResponse r = new MarkResponse();
            r.id = m.getId();
            r.assessmentId = m.getAssessment() != null ? m.getAssessment().getId() : null;
            r.studentId = m.getStudent() != null ? m.getStudent().getId() : null;
            r.marks = m.getMarks();
            r.feedback = m.getFeedback();
            return r;
        }

        public Long getId() { return id; }
        public Long getAssessmentId() { return assessmentId; }
        public Long getStudentId() { return studentId; }
        public int getMarks() { return marks; }
        public String getFeedback() { return feedback; }
    }

}
