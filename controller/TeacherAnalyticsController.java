package com.school.School_Management_System.controller;

import com.school.School_Management_System.dto.ClassSubjectAnalyticsDTO;
import com.school.School_Management_System.model.Mark;
import com.school.School_Management_System.model.Role;
import com.school.School_Management_System.model.User;
import com.school.School_Management_System.repository.EnrollmentRepository;
import com.school.School_Management_System.repository.MarkRepository;
import com.school.School_Management_System.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherAnalyticsController {

    private final UserRepository userRepo;
    private final MarkRepository markRepo;
    private final EnrollmentRepository enrollmentRepo;

    // OPTIONAL: keep this only for response display (not filtering)
    private static final String DISPLAY_ACADEMIC_YEAR = "N/A";

    public TeacherAnalyticsController(UserRepository userRepo,
                                      MarkRepository markRepo,
                                      EnrollmentRepository enrollmentRepo) {
        this.userRepo = userRepo;
        this.markRepo = markRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    // =========================================================
    // 1) Class Analytics: subject-wise average/high/low
    // =========================================================
    @GetMapping("/class-analytics")
    public ClassAnalyticsResponse classAnalytics(@RequestParam Long classroomId,
                                                 Authentication auth) {

        User requester = userRepo.findByUsername(auth.getName()).orElseThrow();

        Set<Long> allowedStudents = getAllowedStudentIds(classroomId);
        List<Mark> marks = markRepo.findByClassroomId(classroomId);

        Map<Long, SubjectBucket> subjectMap = new LinkedHashMap<>();

        for (Mark m : marks) {
            if (m.getStudent() == null || m.getStudent().getRole() != Role.STUDENT) continue;
            if (m.getAssessment() == null || m.getAssessment().getSubject() == null) continue;

            Long studentId = m.getStudent().getId();
            if (!allowedStudents.contains(studentId)) continue;

            Long subjectId = m.getAssessment().getSubject().getId();
            String subjectName = m.getAssessment().getSubject().getName();

            subjectMap.computeIfAbsent(subjectId, k -> new SubjectBucket(subjectId, subjectName))
                    .add(studentId, m.getMarks(), m.getAssessment().getMaxMarks());
        }

        List<ClassSubjectAnalyticsDTO> subjects = new ArrayList<>();
        double overallSum = 0;
        int overallCount = 0;

        for (SubjectBucket sb : subjectMap.values()) {
            List<Double> percents = sb.studentPercentages();

            double avg = percents.stream().mapToDouble(d -> d).average().orElse(0);
            double high = percents.stream().mapToDouble(d -> d).max().orElse(0);
            double low = percents.stream().mapToDouble(d -> d).min().orElse(0);

            subjects.add(new ClassSubjectAnalyticsDTO(
                    sb.subjectId,
                    sb.subjectName,
                    percents.size(),
                    round2(avg),
                    round2(high),
                    round2(low)
            ));

            overallSum += avg;
            overallCount++;
        }

        double overallAvg = overallCount == 0 ? 0 : (overallSum / overallCount);

        return new ClassAnalyticsResponse(
                classroomId,
                DISPLAY_ACADEMIC_YEAR,
                requester.getUsername(),
                round2(overallAvg),
                subjects
        );
    }

    // =========================================================
    // 2) Class Rank
    // =========================================================
    @GetMapping("/class-rank")
    public List<ClassRankRow> classRank(@RequestParam Long classroomId,
                                        Authentication auth) {

        userRepo.findByUsername(auth.getName()).orElseThrow(); // ensure logged-in

        Set<Long> allowedStudents = getAllowedStudentIds(classroomId);
        List<Mark> marks = markRepo.findByClassroomId(classroomId);

        Map<Long, int[]> totals = new HashMap<>();
        Map<Long, String> usernames = new HashMap<>();

        for (Mark m : marks) {
            if (m.getStudent() == null || m.getStudent().getRole() != Role.STUDENT) continue;
            if (m.getAssessment() == null) continue;

            Long studentId = m.getStudent().getId();
            if (!allowedStudents.contains(studentId)) continue;

            usernames.put(studentId, m.getStudent().getUsername());

            int[] t = totals.computeIfAbsent(studentId, k -> new int[]{0, 0});
            t[0] += m.getMarks();
            t[1] += m.getAssessment().getMaxMarks();
        }

        List<ClassRankRow> rows = new ArrayList<>();

        for (var e : totals.entrySet()) {
            Long studentId = e.getKey();
            int obtained = e.getValue()[0];
            int max = e.getValue()[1];
            double percent = max == 0 ? 0 : (obtained * 100.0 / max);

            rows.add(new ClassRankRow(
                    studentId,
                    usernames.get(studentId),
                    obtained,
                    max,
                    round2(percent),
                    null
            ));
        }

        rows.sort((a, b) -> Double.compare(b.percentage, a.percentage));

        int rank = 1;
        for (ClassRankRow row : rows) row.rank = rank++;

        return rows;
    }

    // =========================================================
// 3) Pass Rate  (PASS if >= 35% of total max marks)
// =========================================================
    @GetMapping("/pass-rate")
    public PassRateResponse passRate(@RequestParam Long classroomId,
                                     Authentication auth) {

        userRepo.findByUsername(auth.getName()).orElseThrow();

        // ✅ PASS threshold = 35%
        final double PASS_PERCENT = 35.0;

        Set<Long> allowedStudents = getAllowedStudentIds(classroomId);

        // marks for this classroom
        List<Mark> marks = markRepo.findByClassroomId(classroomId);

        // studentId -> [obtainedSum, maxSum]
        Map<Long, int[]> totals = new HashMap<>();

        for (Mark m : marks) {
            if (m == null) continue;
            if (m.getStudent() == null || m.getStudent().getRole() != Role.STUDENT) continue;
            if (m.getAssessment() == null) continue;

            Long studentId = m.getStudent().getId();
            if (!allowedStudents.contains(studentId)) continue;

            int obtained = m.getMarks();
            int max = m.getAssessment().getMaxMarks();

            // safety
            if (max <= 0) continue;

            int[] t = totals.computeIfAbsent(studentId, k -> new int[]{0, 0});
            t[0] += obtained;
            t[1] += max;
        }

        int totalStudents = totals.size();
        int passed = 0;

        for (int[] t : totals.values()) {
            int obtainedSum = t[0];
            int maxSum = t[1];

            double percent = maxSum == 0 ? 0.0 : (obtainedSum * 100.0 / maxSum);

            if (percent >= PASS_PERCENT) passed++;
        }

        int failed = totalStudents - passed;

        double passPercent = totalStudents == 0 ? 0.0 : (passed * 100.0 / totalStudents);

        return new PassRateResponse(
                classroomId,
                DISPLAY_ACADEMIC_YEAR,
                totalStudents,
                passed,
                failed,
                round2(passPercent)
        );
    }

    // =========================================================
    // 4) Subject Toppers
    // =========================================================
    @GetMapping("/subject-toppers")
    public List<SubjectTopperRow> subjectToppers(@RequestParam Long classroomId,
                                                 @RequestParam Long subjectId,
                                                 @RequestParam(defaultValue = "5") int limit,
                                                 Authentication auth) {

        userRepo.findByUsername(auth.getName()).orElseThrow();

        Set<Long> allowedStudents = getAllowedStudentIds(classroomId);
        List<Mark> marks = markRepo.findByClassroomIdAndSubjectId(classroomId, subjectId);

        Map<Long, int[]> totals = new HashMap<>();
        Map<Long, String> usernames = new HashMap<>();

        for (Mark m : marks) {
            if (m.getStudent() == null || m.getStudent().getRole() != Role.STUDENT) continue;
            if (m.getAssessment() == null) continue;

            Long studentId = m.getStudent().getId();
            if (!allowedStudents.contains(studentId)) continue;

            usernames.put(studentId, m.getStudent().getUsername());

            int[] t = totals.computeIfAbsent(studentId, k -> new int[]{0, 0});
            t[0] += m.getMarks();
            t[1] += m.getAssessment().getMaxMarks();
        }

        List<SubjectTopperRow> rows = new ArrayList<>();

        for (var e : totals.entrySet()) {
            Long studentId = e.getKey();
            int obtained = e.getValue()[0];
            int max = e.getValue()[1];
            double percent = max == 0 ? 0 : (obtained * 100.0 / max);

            rows.add(new SubjectTopperRow(studentId, usernames.get(studentId), obtained, max, round2(percent), null));
        }

        rows.sort((a, b) -> Double.compare(b.percentage, a.percentage));

        int rank = 1;
        for (SubjectTopperRow row : rows) row.rank = rank++;

        if (limit < 1) limit = 1;
        if (rows.size() > limit) return rows.subList(0, limit);
        return rows;
    }

    // =========================================================
    // 5) Dashboard Summary
    // =========================================================
    @GetMapping("/dashboard-summary")
    public DashboardSummaryResponse dashboardSummary(@RequestParam Long classroomId,
                                                     @RequestParam(defaultValue = "3") int topLimit,
                                                     Authentication auth) {

        User requester = userRepo.findByUsername(auth.getName()).orElseThrow();

        ClassAnalyticsResponse analytics = classAnalytics(classroomId, auth);
        PassRateResponse passRate = passRate(classroomId, auth);

        List<ClassRankRow> rank = classRank(classroomId, auth);
        if (topLimit < 1) topLimit = 1;
        if (rank.size() > topLimit) rank = rank.subList(0, topLimit);

        return new DashboardSummaryResponse(
                classroomId,
                DISPLAY_ACADEMIC_YEAR,
                requester.getUsername(),
                analytics.overallAveragePercent(),
                passRate,
                rank,
                analytics.subjects()
        );
    }

    // =========================================================
    // Helpers
    // =========================================================
    private Set<Long> getAllowedStudentIds(Long classroomId) {
        List<Long> ids = enrollmentRepo.findStudentIdsByClassroom(classroomId);
        return new HashSet<>(ids);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static class SubjectBucket {
        Long subjectId;
        String subjectName;
        Map<Long, int[]> totals = new HashMap<>();

        SubjectBucket(Long subjectId, String subjectName) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
        }

        void add(Long studentId, int obt, int max) {
            int[] t = totals.computeIfAbsent(studentId, k -> new int[]{0, 0});
            t[0] += obt;
            t[1] += max;
        }

        List<Double> studentPercentages() {
            List<Double> list = new ArrayList<>();
            for (int[] t : totals.values()) {
                double p = t[1] == 0 ? 0 : (t[0] * 100.0 / t[1]);
                list.add(p);
            }
            return list;
        }
    }

    // =========================================================
    // Response DTOs
    // =========================================================
    public record ClassAnalyticsResponse(
            Long classroomId,
            String academicYear,
            String requestedBy,
            double overallAveragePercent,
            List<ClassSubjectAnalyticsDTO> subjects
    ) {}

    public record PassRateResponse(
            Long classroomId,
            String academicYear,
            int totalStudents,
            int passed,
            int failed,
            double passPercent
    ) {}

    public record DashboardSummaryResponse(
            Long classroomId,
            String academicYear,
            String requestedBy,
            double overallAveragePercent,
            PassRateResponse passRate,
            List<ClassRankRow> topRank,
            List<ClassSubjectAnalyticsDTO> subjectAnalytics
    ) {}

    public static class ClassRankRow {
        public Long studentId;
        public String username;
        public int totalObtained;
        public int totalMax;
        public double percentage;
        public Integer rank;

        public ClassRankRow(Long studentId, String username, int totalObtained, int totalMax, double percentage, Integer rank) {
            this.studentId = studentId;
            this.username = username;
            this.totalObtained = totalObtained;
            this.totalMax = totalMax;
            this.percentage = percentage;
            this.rank = rank;
        }
    }

    public static class SubjectTopperRow {
        public Long studentId;
        public String username;
        public int obtained;
        public int max;
        public double percentage;
        public Integer rank;

        public SubjectTopperRow(Long studentId, String username, int obtained, int max, double percentage, Integer rank) {
            this.studentId = studentId;
            this.username = username;
            this.obtained = obtained;
            this.max = max;
            this.percentage = percentage;
            this.rank = rank;
        }
    }
}
