package com.school.School_Management_System.controller;

import com.school.School_Management_System.dto.CreateEnrollmentRequest;
import com.school.School_Management_System.model.Classroom;
import com.school.School_Management_System.model.Enrollment;
import com.school.School_Management_System.model.Role;
import com.school.School_Management_System.model.User;
import com.school.School_Management_System.repository.ClassroomRepository;
import com.school.School_Management_System.repository.EnrollmentRepository;
import com.school.School_Management_System.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/principal")
public class PrincipalEnrollmentController {

    private final UserRepository userRepo;
    private final ClassroomRepository classroomRepo;
    private final EnrollmentRepository enrollmentRepo;

    public PrincipalEnrollmentController(UserRepository userRepo,
                                         ClassroomRepository classroomRepo,
                                         EnrollmentRepository enrollmentRepo) {
        this.userRepo = userRepo;
        this.classroomRepo = classroomRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    // ✅ Response DTO (prevents Hibernate proxy JSON problems)
    public record EnrollmentResponse(
            Long id,
            Long schoolId,
            Long studentId,
            String studentUsername,
            Long classroomId,
            String classroomName,
            String academicYear
    ) {}

    // ---------------------------
    // POST: Enroll student
    // ---------------------------
    @PostMapping("/enrollments")
    public EnrollmentResponse enroll(@Valid @RequestBody CreateEnrollmentRequest req, Authentication auth) {
        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = principal.getSchool().getId();

        User student = userRepo.findById(req.studentId).orElseThrow();
        Classroom classroom = classroomRepo.findById(req.classroomId).orElseThrow();

        // same school checks
        if (student.getSchool() == null || !student.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Student not in your school");
        }
        if (classroom.getSchool() == null || !classroom.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Classroom not in your school");
        }
        if (student.getRole() != Role.STUDENT) {
            throw new RuntimeException("User is not a student");
        }

        // avoid duplicate enrollment (safer: include schoolId too if you add this method)
        boolean exists = enrollmentRepo.existsByStudent_IdAndClassroom_IdAndAcademicYear(
                student.getId(), classroom.getId(), req.academicYear
        );
        if (exists) throw new RuntimeException("Student already enrolled");

        Enrollment e = new Enrollment();
        e.setSchool(principal.getSchool());
        e.setStudent(student);
        e.setClassroom(classroom);
        e.setAcademicYear(req.academicYear);

        Enrollment saved = enrollmentRepo.save(e);

        return new EnrollmentResponse(
                saved.getId(),
                saved.getSchool().getId(),
                saved.getStudent().getId(),
                saved.getStudent().getUsername(),
                saved.getClassroom().getId(),
                saved.getClassroom().getName(),
                saved.getAcademicYear()
        );
    }

    // ---------------------------
    // GET: list enrollments
    // ---------------------------
    @GetMapping("/enrollments")
    public List<EnrollmentResponse> list(@RequestParam Long classroomId,
                                         @RequestParam String academicYear,
                                         Authentication auth) {

        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = principal.getSchool().getId();

        Classroom classroom = classroomRepo.findById(classroomId).orElseThrow();
        if (classroom.getSchool() == null || !classroom.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Not your school classroom");
        }

        return enrollmentRepo.findByClassroom_IdAndAcademicYear(classroomId, academicYear)
                .stream()
                .map(enr -> new EnrollmentResponse(
                        enr.getId(),
                        enr.getSchool().getId(),
                        enr.getStudent().getId(),
                        enr.getStudent().getUsername(),
                        enr.getClassroom().getId(),
                        enr.getClassroom().getName(),
                        enr.getAcademicYear()
                ))
                .toList();
    }

    // ---------------------------
    // DELETE: enrollment
    // ---------------------------
    @DeleteMapping("/enrollments/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = principal.getSchool().getId();

        Enrollment e = enrollmentRepo.findById(id).orElseThrow();

        if (e.getSchool() == null || !e.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Not your school enrollment");
        }

        enrollmentRepo.delete(e);
    }
}
