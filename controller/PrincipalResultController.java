package com.school.School_Management_System.controller;

import com.school.School_Management_System.dto.PublishResultRequest;
import com.school.School_Management_System.model.Classroom;
import com.school.School_Management_System.model.ResultPublish;
import com.school.School_Management_System.model.School;
import com.school.School_Management_System.model.User;
import com.school.School_Management_System.repository.ClassroomRepository;
import com.school.School_Management_System.repository.ResultPublicationRepository;
import com.school.School_Management_System.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/principal/results")
public class PrincipalResultController {

    private final UserRepository userRepo;
    private final ClassroomRepository classroomRepo;
    private final ResultPublicationRepository publishRepo;

    private static final String TEMP_ACADEMIC_YEAR = "2025-2026";

    public PrincipalResultController(UserRepository userRepo,
                                     ClassroomRepository classroomRepo,
                                     ResultPublicationRepository publishRepo) {
        this.userRepo = userRepo;
        this.classroomRepo = classroomRepo;
        this.publishRepo = publishRepo;
    }

    // =========================================================
    // 1) Publish now OR schedule (Body-based)
    // =========================================================
    @PostMapping("/publish")
    public PublishResponse publish(@Valid @RequestBody PublishResultRequest req,
                                   Authentication auth) {

        User principal = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        School school = principal.getSchool();
        if (school == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Principal has no school");
        }

        Classroom classroom = classroomRepo.findById(req.getClassroomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom not found"));

        // ensure classroom belongs to principal school
        if (classroom.getSchool() == null || !classroom.getSchool().getId().equals(school.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your school classroom");
        }

        String academicYear = normalizeYear(req.getAcademicYear());

        ResultPublish rp = publishRepo
                .findBySchool_IdAndClassroom_IdAndAcademicYear(school.getId(), classroom.getId(), academicYear)
                .orElseGet(ResultPublish::new);

        rp.setSchool(school);
        rp.setClassroom(classroom);
        rp.setAcademicYear(academicYear);
        rp.setPublishedBy(principal);

        // publish now vs schedule
        LocalDateTime publishAt = req.getPublishAt(); // ✅ make this LocalDateTime in DTO (recommended)
        if (publishAt != null) {
            rp.setPublishAt(publishAt);
            rp.setPublished(false);
            rp.setPublishedAt(null);
        } else {
            publishNow(rp);
        }

        ResultPublish saved = publishRepo.save(rp);
        return PublishResponse.from(saved, isEffectivelyPublished(saved));
    }

    // =========================================================
    // 2) Unpublish
    // =========================================================
    @DeleteMapping("/publish")
    public PublishResponse unpublish(@RequestParam Long classroomId,
                                     @RequestParam(required = false) String academicYear,
                                     Authentication auth) {

        User principal = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        School school = principal.getSchool();

        String year = normalizeYear(academicYear);

        ResultPublish rp = publishRepo
                .findBySchool_IdAndClassroom_IdAndAcademicYear(school.getId(), classroomId, year)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Result publish record not found"));

        // extra safety
        if (rp.getClassroom() == null || rp.getClassroom().getSchool() == null
                || !rp.getClassroom().getSchool().getId().equals(school.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your school classroom");
        }

        rp.setPublished(false);
        rp.setPublishAt(null);
        rp.setPublishedAt(null);
        rp.setPublishedBy(principal);

        ResultPublish saved = publishRepo.save(rp);
        return PublishResponse.from(saved, false);
    }

    // =========================================================
    // 3) Status (rich response)
    // =========================================================
    @GetMapping("/status")
    public PublishResponse status(@RequestParam Long classroomId,
                                  @RequestParam(required = false) String academicYear,
                                  Authentication auth) {

        User principal = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        String year = normalizeYear(academicYear);

        ResultPublish rp = publishRepo
                .findBySchool_IdAndClassroom_IdAndAcademicYear(
                        principal.getSchool().getId(),
                        classroomId,
                        year
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No publish record found"));

        // ✅ if schedule time passed, treat as published (and optionally auto-update DB)
        if (!rp.isPublished() && rp.getPublishAt() != null && !rp.getPublishAt().isAfter(LocalDateTime.now())) {
            publishNow(rp);
            rp = publishRepo.save(rp);
        }

        return PublishResponse.from(rp, isEffectivelyPublished(rp));
    }

    // =========================================================
    // Helpers
    // =========================================================
    private static String normalizeYear(String year) {
        return (year == null || year.isBlank()) ? TEMP_ACADEMIC_YEAR : year.trim();
    }

    private static void publishNow(ResultPublish rp) {
        rp.setPublishAt(null);
        rp.setPublished(true);
        rp.setPublishedAt(LocalDateTime.now());
    }

    // "Effective" published state (supports schedule)
    private static boolean isEffectivelyPublished(ResultPublish rp) {
        if (rp.isPublished()) return true;
        return rp.getPublishAt() != null && !rp.getPublishAt().isAfter(LocalDateTime.now());
    }

    // =========================================================
    // Response DTO
    // =========================================================
    public static class PublishResponse {
        public Long id;
        public Long classroomId;
        public String academicYear;

        // actual saved fields
        public boolean published;
        public LocalDateTime publishAt;
        public LocalDateTime publishedAt;

        // computed field for frontend convenience
        public boolean effectivePublished;

        public static PublishResponse from(ResultPublish r, boolean effectivePublished) {
            PublishResponse p = new PublishResponse();
            p.id = r.getId();
            p.classroomId = r.getClassroom() != null ? r.getClassroom().getId() : null;
            p.academicYear = r.getAcademicYear();
            p.published = r.isPublished();
            p.publishAt = r.getPublishAt();
            p.publishedAt = r.getPublishedAt();
            p.effectivePublished = effectivePublished;
            return p;
        }
    }
}
