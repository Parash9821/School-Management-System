package com.school.School_Management_System.controller;

import com.school.School_Management_System.model.School;
import com.school.School_Management_System.model.Subject;
import com.school.School_Management_System.model.User;
import com.school.School_Management_System.repository.SchoolRepository;
import com.school.School_Management_System.repository.SubjectRepository;
import com.school.School_Management_System.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/principal/subjects")
public class PrincipalSubjectController {

    private final UserRepository userRepo;
    private final SchoolRepository schoolRepo;
    private final SubjectRepository subjectRepo;

    public PrincipalSubjectController(UserRepository userRepo,
                                      SchoolRepository schoolRepo,
                                      SubjectRepository subjectRepo) {
        this.userRepo = userRepo;
        this.schoolRepo = schoolRepo;
        this.subjectRepo = subjectRepo;
    }

    // -----------------------
    // DTOs
    // -----------------------
    public record SubjectRequest(
            @NotBlank String name,
            @NotBlank String code
    ) {}

    public record SubjectResponse(
            Long id,
            String name,
            String code
    ) {}

    // -----------------------
    // Helpers
    // -----------------------
    private School principalSchool(Authentication auth) {
        User principal = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (principal.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Principal has no school assigned");
        }

        // ensure managed entity if needed
        return schoolRepo.findById(principal.getSchool().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
    }

    private static SubjectResponse toResponse(Subject s) {
        return new SubjectResponse(s.getId(), s.getName(), s.getCode());
    }

    private static String normCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    // -----------------------
    // APIs
    // -----------------------

    // GET /api/principal/subjects
    @GetMapping
    public List<SubjectResponse> list(Authentication auth) {
        School school = principalSchool(auth);
        return subjectRepo.findBySchool_IdOrderByIdAsc(school.getId())
                .stream()
                .map(PrincipalSubjectController::toResponse)
                .toList();
    }

    // POST /api/principal/subjects
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectResponse create(@Valid @RequestBody SubjectRequest body,
                                  Authentication auth) {

        School school = principalSchool(auth);

        String code = normCode(body.code());
        if (subjectRepo.existsBySchool_IdAndCode(school.getId(), code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Subject code already exists in your school");
        }

        Subject s = new Subject();
        s.setName(body.name().trim());
        s.setCode(code);
        s.setSchool(school);

        return toResponse(subjectRepo.save(s));
    }

    // PUT /api/principal/subjects/{id}
    @PutMapping("/{id}")
    public SubjectResponse update(@PathVariable Long id,
                                  @Valid @RequestBody SubjectRequest body,
                                  Authentication auth) {

        School school = principalSchool(auth);

        Subject s = subjectRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        if (!s.getSchool().getId().equals(school.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your school subject");
        }

        String newCode = normCode(body.code());
        if (!s.getCode().equalsIgnoreCase(newCode)
                && subjectRepo.existsBySchool_IdAndCode(school.getId(), newCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Subject code already exists in your school");
        }

        s.setName(body.name().trim());
        s.setCode(newCode);

        return toResponse(subjectRepo.save(s));
    }

    // DELETE /api/principal/subjects/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        School school = principalSchool(auth);

        Subject s = subjectRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        if (!s.getSchool().getId().equals(school.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your school subject");
        }

        subjectRepo.delete(s);
    }
}
