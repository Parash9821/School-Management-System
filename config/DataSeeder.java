package com.school.School_Management_System.config;

import com.school.School_Management_System.model.*;
import com.school.School_Management_System.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final SchoolRepository schoolRepo;
    private final UserRepository userRepo;
    private final ClassroomRepository classroomRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(SchoolRepository schoolRepo,
                      UserRepository userRepo,
                      ClassroomRepository classroomRepo,
                      EnrollmentRepository enrollmentRepo,
                      PasswordEncoder passwordEncoder) {
        this.schoolRepo = schoolRepo;
        this.userRepo = userRepo;
        this.classroomRepo = classroomRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        final String academicYear = "2025-2026";

        final String schoolName = "Default School";
        School school = getOrCreateSchoolWithSlug(schoolName);

        final String classroomName = "Grade 10 A";
        Classroom classroom = getOrCreateClassroom(school, classroomName);

        // core users
        createUserIfMissing(school, "principal", "Principal User", Role.PRINCIPAL, "123456");
        createUserIfMissing(school, "teacher", "Teacher User", Role.TEACHER, "123456");

        // students
        for (int i = 1; i <= 11; i++) {
            createUserIfMissing(school, "student" + i, "Student " + i, Role.STUDENT, "123456");
        }

        // enroll students
        for (int i = 1; i <= 11; i++) {
            String uname = "student" + i;

            User st = userRepo.findBySchool_IdAndUsername(school.getId(), uname)
                    .orElseThrow(() -> new RuntimeException(
                            "Seeder: " + uname + " not found in schoolId=" + school.getId()));

            enrollIfMissing(school, classroom, st, academicYear);
        }

        System.out.println("✅ Seed completed: school=" + school.getName()
                + " (slug=" + safeGetSlug(school) + ")"
                + ", classroom=" + classroom.getName()
                + ", year=" + academicYear);
    }

    // ----------------------------
    // School/Classroom helpers
    // ----------------------------
    private School getOrCreateSchoolWithSlug(String name) {
        // If you have findBySlug or findByName in repo, use it.
        // For now, safely search all schools.
        Optional<School> existing = schoolRepo.findAll()
                .stream()
                .filter(s -> name.equalsIgnoreCase(s.getName()))
                .findFirst();

        String slug = toSlug(name);

        if (existing.isPresent()) {
            School s = existing.get();

            // If existing row has null/blank slug, fix it (prevents future errors)
            if (safeGetSlug(s) == null || safeGetSlug(s).isBlank()) {
                setSlug(s, slug);
                return schoolRepo.save(s);
            }
            return s;
        }

        School s = new School();
        s.setName(name);
        setSlug(s, slug);          // ✅ IMPORTANT FIX
        // if your School has these optional fields, you can leave them null:
        // s.setAboutHtml(null);
        // s.setBrandingJson(null);

        return schoolRepo.save(s);
    }

    private Classroom getOrCreateClassroom(School school, String classroomName) {
        return classroomRepo.findBySchool_Id(school.getId())
                .stream()
                .filter(c -> classroomName.equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Classroom c = new Classroom();
                    c.setSchool(school);
                    c.setName(classroomName);
                    return classroomRepo.save(c);
                });
    }

    // ----------------------------
    // User/Enrollment helpers
    // ----------------------------
    private void createUserIfMissing(School school,
                                     String username,
                                     String fullName,
                                     Role role,
                                     String rawPassword) {

        Long schoolId = school.getId();
        if (userRepo.existsBySchool_IdAndUsername(schoolId, username)) return;

        User u = new User();
        u.setSchool(school);
        u.setUsername(username);
        u.setFullName(fullName);
        u.setRole(role);
        u.setEnabled(true);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));

        userRepo.save(u);
    }

    private void enrollIfMissing(School school, Classroom classroom, User student, String academicYear) {
        if (student.getRole() != Role.STUDENT) return;

        boolean exists = enrollmentRepo.existsBySchool_IdAndStudent_IdAndClassroom_IdAndAcademicYear(
                school.getId(), student.getId(), classroom.getId(), academicYear
        );
        if (exists) return;

        Enrollment e = new Enrollment();
        e.setSchool(school);
        e.setStudent(student);
        e.setClassroom(classroom);
        e.setAcademicYear(academicYear);

        enrollmentRepo.save(e);
    }

    // ----------------------------
    // Slug helpers
    // ----------------------------
    private String toSlug(String input) {
        if (input == null) return "school";
        String slug = input.trim().toLowerCase();
        slug = slug.replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("(^-+|-+$)", "");
        return slug.isBlank() ? "school" : slug;
    }

    // These two small methods avoid compilation problems if your School uses different field names.
    // Replace them if your getters/setters are different.
    private String safeGetSlug(School s) {
        try {
            return (String) School.class.getMethod("getSlug").invoke(s);
        } catch (Exception ex) {
            return null;
        }
    }

    private void setSlug(School s, String slug) {
        try {
            School.class.getMethod("setSlug", String.class).invoke(s, slug);
        } catch (Exception ex) {
            // If your School class does not have slug setters, then you MUST add them.
            throw new RuntimeException("School has no setSlug(String). Please add slug field + getter/setter in School entity.");
        }
    }
}
