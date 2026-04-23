package com.school.School_Management_System.controller;

import com.school.School_Management_System.dto.StudentSubjectResultDTO;
import com.school.School_Management_System.model.*;
import com.school.School_Management_System.repository.*;
import com.school.School_Management_System.service.GradingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentResultController {

    private final UserRepository userRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final ResultPublicationRepository publishRepo;
    private final MarkRepository markRepo;
    private final GradingService gradingService;

    private static final String TEMP_ACADEMIC_YEAR = "2025-2026";

    public StudentResultController(UserRepository userRepo,
                                   EnrollmentRepository enrollmentRepo,
                                   ResultPublicationRepository publishRepo,
                                   MarkRepository markRepo,
                                   GradingService gradingService) {
        this.userRepo = userRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.publishRepo = publishRepo;
        this.markRepo = markRepo;
        this.gradingService = gradingService;
    }

    // =========================================================
    // 1) SUMMARY (overall only)
    // GET /api/student/result-summary?academicYear=2025-2026
    // =========================================================
    @GetMapping("/result-summary")
    public ResultSummaryResponse summary(@RequestParam(required = false) String academicYear,
                                         Authentication auth) {

        StudentContext ctx = studentContext(auth, academicYear);
        List<Mark> marks = marksForStudentInClassroom(ctx.student.getId(), ctx.classroomId);

        Totals totals = computeTotals(marks);

        double percent = totals.totalMax == 0 ? 0 : (totals.totalObtained * 100.0 / totals.totalMax);

        return new ResultSummaryResponse(
                ctx.student.getUsername(),
                ctx.academicYear,
                totals.totalObtained,
                totals.totalMax,
                round2(percent)
        );
    }

    // =========================================================
    // 2) ANALYTICS (subject-wise + overall grade/pass)
    // GET /api/student/result-analytics?academicYear=2025-2026
    // =========================================================
    @GetMapping("/result-analytics")
    public StudentAnalyticsResponse analytics(@RequestParam(required = false) String academicYear,
                                              Authentication auth) {

        StudentContext ctx = studentContext(auth, academicYear);
        List<Mark> marks = marksForStudentInClassroom(ctx.student.getId(), ctx.classroomId);

        SubjectAndTotals st = computeSubjectWise(marks);

        double overallPercent = st.totalMax == 0 ? 0 : (st.totalObt * 100.0 / st.totalMax);
        String overallGrade = gradingService.gradeFromPercentage(overallPercent);
        boolean overallPass = gradingService.isPass(overallPercent);

        List<StudentSubjectResultDTO> subjects = new ArrayList<>();
        for (SubjectAgg ag : st.subjects.values()) {
            double percent = ag.max == 0 ? 0 : (ag.obt * 100.0 / ag.max);
            subjects.add(new StudentSubjectResultDTO(
                    ag.subjectId,
                    ag.subjectName,
                    ag.obt,
                    ag.max,
                    round2(percent),
                    gradingService.gradeFromPercentage(percent),
                    gradingService.isPass(percent)
            ));
        }

        return new StudentAnalyticsResponse(
                ctx.student.getUsername(),
                ctx.academicYear,
                round2(overallPercent),
                overallGrade,
                overallPass,
                subjects
        );
    }

    // =========================================================
    // 3) FULL RESULTS (subject-wise + totals + grades + publish info)
    // GET /api/student/results?academicYear=2025-2026
    // =========================================================
    @GetMapping("/results")
    public StudentResultResponse myResults(@RequestParam(required = false) String academicYear,
                                           Authentication auth) {

        StudentContext ctx = studentContext(auth, academicYear);

        // marks filtered by classroom
        List<Mark> marks = marksForStudentInClassroom(ctx.student.getId(), ctx.classroomId);

        SubjectAndTotals st = computeSubjectWise(marks);

        List<SubjectRow> rows = new ArrayList<>();
        for (SubjectAgg ag : st.subjects.values()) {
            double percent = ag.max == 0 ? 0 : (ag.obt * 100.0 / ag.max);
            rows.add(new SubjectRow(
                    ag.subjectId,
                    ag.subjectName,
                    ag.obt,
                    ag.max,
                    round2(percent),
                    gradingService.gradeFromPercentage(percent),
                    gradingService.isPass(percent)
            ));
        }

        double overallPercent = st.totalMax == 0 ? 0 : (st.totalObt * 100.0 / st.totalMax);
        String overallGrade = gradingService.gradeFromPercentage(overallPercent);
        boolean overallPass = gradingService.isPass(overallPercent);

        return new StudentResultResponse(
                ctx.student.getId(),
                ctx.student.getUsername(),
                ctx.student.getFullName(),
                ctx.classroomId,
                ctx.classroomName,
                ctx.academicYear,
                ctx.publishedAt,
                true, // effective published already checked
                st.totalObt,
                st.totalMax,
                round2(overallPercent),
                overallGrade,
                overallPass,
                rows
        );
    }

    // =========================================================
    // Student context + publish checks (Effective published logic)
    // =========================================================
    private StudentContext studentContext(Authentication auth, String academicYearParam) {
        User student = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only STUDENT can access");
        }
        if (student.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student has no school");
        }

        Long schoolId = student.getSchool().getId();
        String year = normalizeYear(academicYearParam);

        Enrollment enr = enrollmentRepo.findBySchool_IdAndStudent_Id(schoolId, student.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not enrolled"));

        Long classroomId = enr.getClassroom().getId();
        String classroomName = enr.getClassroom().getName();

        ResultPublish rp = publishRepo
                .findBySchool_IdAndClassroom_IdAndAcademicYear(schoolId, classroomId, year)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Result not published yet"));

        // effective published check
        if (!isEffectivelyPublished(rp)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Result not published yet");
        }

        // If scheduled time passed but not marked published, optional auto-fix:
        if (!rp.isPublished() && rp.getPublishAt() != null && !rp.getPublishAt().isAfter(LocalDateTime.now())) {
            rp.setPublishAt(null);
            rp.setPublished(true);
            rp.setPublishedAt(LocalDateTime.now());
            rp = publishRepo.save(rp);
        }

        return new StudentContext(student, classroomId, classroomName, year, rp.getPublishedAt());
    }

    private List<Mark> marksForStudentInClassroom(Long studentId, Long classroomId) {
        // Use whichever method you have in your repo:
        // - findByStudent_Id(studentId)
        // - findByStudentId(studentId)
        // Keep ONE in your MarkRepository and use it consistently.
        List<Mark> marks = markRepo.findByStudent_Id(studentId);

        // filter by classroom
        List<Mark> filtered = new ArrayList<>();
        for (Mark m : marks) {
            if (m.getAssessment() == null) continue;
            if (m.getAssessment().getClassroom() == null) continue;
            if (!Objects.equals(m.getAssessment().getClassroom().getId(), classroomId)) continue;
            if (m.getAssessment().getSubject() == null) continue;
            filtered.add(m);
        }
        return filtered;
    }

    private static String normalizeYear(String year) {
        return (year == null || year.isBlank()) ? TEMP_ACADEMIC_YEAR : year.trim();
    }

    private static boolean isEffectivelyPublished(ResultPublish rp) {
        if (rp.isPublished()) return true;
        LocalDateTime at = rp.getPublishAt();
        return at != null && !at.isAfter(LocalDateTime.now());
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // =========================================================
    // Computation helpers
    // =========================================================
    private Totals computeTotals(List<Mark> marks) {
        int totalObtained = 0;
        int totalMax = 0;

        for (Mark m : marks) {
            int max = m.getAssessment().getMaxMarks();
            if (max <= 0) continue;
            totalObtained += m.getMarks();
            totalMax += max;
        }
        return new Totals(totalObtained, totalMax);
    }

    private SubjectAndTotals computeSubjectWise(List<Mark> marks) {
        Map<Long, SubjectAgg> map = new LinkedHashMap<>();
        int totalObt = 0;
        int totalMax = 0;

        for (Mark m : marks) {
            Subject subject = m.getAssessment().getSubject();
            Long sid = subject.getId();
            String sname = subject.getName();

            SubjectAgg ag = map.computeIfAbsent(sid, k -> new SubjectAgg(sid, sname));
            ag.add(m.getMarks(), m.getAssessment().getMaxMarks());

            totalObt += m.getMarks();
            totalMax += m.getAssessment().getMaxMarks();
        }

        return new SubjectAndTotals(map, totalObt, totalMax);
    }

    private record StudentContext(
            User student,
            Long classroomId,
            String classroomName,
            String academicYear,
            LocalDateTime publishedAt
    ) {}

    private record Totals(int totalObtained, int totalMax) {}

    private record SubjectAndTotals(Map<Long, SubjectAgg> subjects, int totalObt, int totalMax) {}

    private static class SubjectAgg {
        Long subjectId;
        String subjectName;
        int obt = 0;
        int max = 0;

        SubjectAgg(Long subjectId, String subjectName) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
        }

        void add(int obtained, int maxMarks) {
            this.obt += obtained;
            this.max += maxMarks;
        }
    }

    // =========================================================
    // Response DTOs
    // =========================================================
    public record ResultSummaryResponse(
            String studentUsername,
            String academicYear,
            int totalObtained,
            int totalMax,
            double percentage
    ) {}

    public record StudentAnalyticsResponse(
            String studentUsername,
            String academicYear,
            double overallPercentage,
            String overallGrade,
            boolean pass,
            List<StudentSubjectResultDTO> subjects
    ) {}

    public record SubjectRow(
            Long subjectId,
            String subjectName,
            int obtained,
            int max,
            double percentage,
            String grade,
            boolean pass
    ) {}

    public record StudentResultResponse(
            Long studentId,
            String username,
            String fullName,
            Long classroomId,
            String classroomName,
            String academicYear,
            LocalDateTime publishedAt,
            boolean published,
            int totalObtained,
            int totalMax,
            double overallPercentage,
            String overallGrade,
            boolean overallPass,
            List<SubjectRow> subjects
    ) {}



}
