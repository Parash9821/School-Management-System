package com.school.School_Management_System.controller;

import com.school.School_Management_System.dto.ClassroomResponse;
import com.school.School_Management_System.model.Classroom;
import com.school.School_Management_System.model.User;
import com.school.School_Management_System.repository.ClassroomRepository;
import com.school.School_Management_System.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/principal")
public class PrincipalClassroomController {

    private final UserRepository userRepo;
    private final ClassroomRepository classroomRepo;

    public PrincipalClassroomController(UserRepository userRepo, ClassroomRepository classroomRepo) {
        this.userRepo = userRepo;
        this.classroomRepo = classroomRepo;
    }

    // ----------- DTO request -----------
    public static class CreateClassroomRequest {
        @NotBlank
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    private ClassroomResponse toResponse(Classroom c) {
        return new ClassroomResponse(
                c.getId(),
                c.getSchool() == null ? null : c.getSchool().getId(),
                c.getName()
        );
    }

    @PostMapping("/classrooms")
    public ClassroomResponse create(@Valid @RequestBody CreateClassroomRequest req,
                                    Authentication auth) {

        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();

        Classroom c = new Classroom();
        c.setSchool(principal.getSchool());
        c.setName(req.getName());

        Classroom saved = classroomRepo.save(c);

        // ✅ return DTO only
        return toResponse(saved);
    }

    @GetMapping("/classrooms")
    public List<ClassroomResponse> list(Authentication auth) {
        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = principal.getSchool().getId();

        return classroomRepo.findBySchool_Id(schoolId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    // PrincipalClassroomController.java
    @DeleteMapping("/classrooms/{id}")
    public void deleteClassroom(@PathVariable Long id, Authentication auth) {
        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();

        Classroom classroom = classroomRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Classroom not found"));

        // security: principal can delete only within their school
        if (!classroom.getSchool().getId().equals(principal.getSchool().getId())) {
            throw new IllegalStateException("Not your school classroom");
        }

        classroomRepo.delete(classroom);
    }

}
