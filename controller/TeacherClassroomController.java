package com.school.School_Management_System.controller;

import com.school.School_Management_System.model.User;
import com.school.School_Management_System.repository.EnrollmentRepository;
import com.school.School_Management_System.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherClassroomController {

    private final UserRepository userRepo;
    private final EnrollmentRepository enrollmentRepo;

    public TeacherClassroomController(UserRepository userRepo,
                                      EnrollmentRepository enrollmentRepo) {
        this.userRepo = userRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    // -----------------------------------------
    // GET /api/teacher/classrooms/{classroomId}/students
    // Returns students enrolled in this classroom (same school as teacher)
    // -----------------------------------------
    @GetMapping("/classrooms/{classroomId}/students")
    public List<StudentResponse> students(@PathVariable Long classroomId,
                                          Authentication auth) {

        User teacher = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = teacher.getSchool().getId();

        return enrollmentRepo
                .findBySchool_IdAndClassroom_Id(schoolId, classroomId)
                .stream()
                .map(enrollment -> {
                    User s = enrollment.getStudent();
                    return new StudentResponse(
                            s.getId(),
                            s.getUsername(),
                            s.getFullName()
                    );
                })
                .toList();
    }

    // -----------------------------------------
    // DTO
    // -----------------------------------------
    public record StudentResponse(
            Long id,
            String username,
            String fullName
    ) {}
}
