package com.school.School_Management_System.controller;

import com.school.School_Management_System.model.TeachingAssignment;
import com.school.School_Management_System.model.User;
import com.school.School_Management_System.repository.TeachingAssignmentRepository;
import com.school.School_Management_System.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherAssignmentsController {

    private final UserRepository userRepo;
    private final TeachingAssignmentRepository assignmentRepo;

    public TeacherAssignmentsController(UserRepository userRepo,
                                        TeachingAssignmentRepository assignmentRepo) {
        this.userRepo = userRepo;
        this.assignmentRepo = assignmentRepo;
    }

    // ✅ GET /api/teacher/assignments
    @GetMapping("/assignments")
    public List<TeacherAssignmentResponse> myAssignments(Authentication auth) {
        User teacher = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = teacher.getSchool().getId();

        List<TeachingAssignment> list =
                assignmentRepo.findBySchool_IdAndTeacher_Id(schoolId, teacher.getId());

        return list.stream().map(TeacherAssignmentResponse::from).toList();
    }

    public record TeacherAssignmentResponse(
            Long id,
            Long classroomId,
            String classroomName,
            Long subjectId,
            String subjectName
    ) {
        public static TeacherAssignmentResponse from(TeachingAssignment ta) {
            return new TeacherAssignmentResponse(
                    ta.getId(),
                    ta.getClassroom().getId(),
                    ta.getClassroom().getName(),
                    ta.getSubject().getId(),
                    ta.getSubject().getName()
            );
        }
    }

}
