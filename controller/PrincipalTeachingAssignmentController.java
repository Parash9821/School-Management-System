package com.school.School_Management_System.controller;

import com.school.School_Management_System.model.*;
import com.school.School_Management_System.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/principal")
public class PrincipalTeachingAssignmentController {

    private final UserRepository userRepo;
    private final ClassroomRepository classroomRepo;
    private final SubjectRepository subjectRepo;
    private final TeachingAssignmentRepository assignmentRepo; // ✅ FIXED

    public PrincipalTeachingAssignmentController(UserRepository userRepo,
                                                 ClassroomRepository classroomRepo,
                                                 SubjectRepository subjectRepo,
                                                 TeachingAssignmentRepository assignmentRepo) { // ✅ FIXED
        this.userRepo = userRepo;
        this.classroomRepo = classroomRepo;
        this.subjectRepo = subjectRepo;
        this.assignmentRepo = assignmentRepo;
    }

    // ----------------------------
    // Request DTO
    // ----------------------------
    public static class CreateAssignmentRequest {
        @NotNull private Long teacherId;
        @NotNull private Long classroomId;
        @NotNull private Long subjectId;

        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

        public Long getClassroomId() { return classroomId; }
        public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    }

    // ----------------------------
    // Response DTO
    // ----------------------------
    public record AssignmentResponse(
            Long id,
            Long schoolId,
            Long teacherId,
            String teacherUsername,
            Long classroomId,
            String classroomName,
            Long subjectId,
            String subjectName
    ) {}

    // ✅ POST: Create assignment
    @PostMapping("/assignments")
    public AssignmentResponse create(@Valid @RequestBody CreateAssignmentRequest req,
                                     Authentication auth) {

        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = principal.getSchool().getId();

        User teacher = userRepo.findById(req.getTeacherId()).orElseThrow();
        if (teacher.getSchool() == null || !teacher.getSchool().getId().equals(schoolId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teacher must belong to the same school");
        }
        if (teacher.getRole() != Role.TEACHER && teacher.getRole() != Role.DEPARTMENT_HEAD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a TEACHER/DEPARTMENT_HEAD");
        }

        Classroom classroom = classroomRepo.findById(req.getClassroomId()).orElseThrow();
        if (classroom.getSchool() == null || !classroom.getSchool().getId().equals(schoolId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Classroom must belong to the same school");
        }

        Subject subject = subjectRepo.findById(req.getSubjectId()).orElseThrow();
        if (subject.getSchool() == null || !subject.getSchool().getId().equals(schoolId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject must belong to the same school");
        }

        // ✅ prevent duplicates (school + classroom + subject)
        if (assignmentRepo.existsBySchool_IdAndClassroom_IdAndSubject_Id(schoolId, classroom.getId(), subject.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assignment already exists for this classroom + subject");
        }

        TeachingAssignment ta = new TeachingAssignment();
        ta.setSchool(principal.getSchool());
        ta.setTeacher(teacher);
        ta.setClassroom(classroom);
        ta.setSubject(subject);

        TeachingAssignment saved = assignmentRepo.save(ta);
        return toResponse(saved);
    }

    // ✅ GET: list assignments by classroom
    @GetMapping("/assignments")
    public List<AssignmentResponse> list(@RequestParam Long classroomId,
                                         Authentication auth) {

        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = principal.getSchool().getId();

        return assignmentRepo.findBySchool_IdAndClassroom_Id(schoolId, classroomId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ✅ DELETE: delete assignment by id
    @DeleteMapping("/assignments/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {

        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = principal.getSchool().getId();

        TeachingAssignment ta = assignmentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

        if (ta.getSchool() == null || !ta.getSchool().getId().equals(schoolId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your school assignment");
        }

        assignmentRepo.delete(ta);
    }

    private AssignmentResponse toResponse(TeachingAssignment ta) {
        return new AssignmentResponse(
                ta.getId(),
                ta.getSchool() != null ? ta.getSchool().getId() : null,
                ta.getTeacher() != null ? ta.getTeacher().getId() : null,
                ta.getTeacher() != null ? ta.getTeacher().getUsername() : null,
                ta.getClassroom() != null ? ta.getClassroom().getId() : null,
                ta.getClassroom() != null ? ta.getClassroom().getName() : null,
                ta.getSubject() != null ? ta.getSubject().getId() : null,
                ta.getSubject() != null ? ta.getSubject().getName() : null
        );
    }
}
