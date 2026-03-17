package com.school.School_Management_System.controller;

import com.school.School_Management_System.dto.CreateUserRequest;
import com.school.School_Management_System.dto.UserResponse;
import com.school.School_Management_System.model.Role;
import com.school.School_Management_System.model.User;
import com.school.School_Management_System.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/principal")
public class PrincipalUsersController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public PrincipalUsersController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ Create student/teacher/department head inside principal's school
    @PostMapping("/users")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest req, Authentication auth) {

        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = principal.getSchool().getId();

        // principal should not create PRINCIPAL accounts from here (keep it safe)
        if (req.getRole() == Role.PRINCIPAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create PRINCIPAL from this endpoint");
        }

        // username unique per school
        if (userRepo.existsBySchool_IdAndUsername(schoolId, req.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists in your school");
        }

        User u = new User();
        u.setSchool(principal.getSchool());
        u.setUsername(req.getUsername());
        u.setFullName(req.getFullName());
        u.setRole(req.getRole());
        u.setEnabled(true);

        // IMPORTANT: store hashed password
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        User saved = userRepo.save(u);
        return UserResponse.from(saved);
    }

    // ✅ List users in principal's school (optional filter by role)
    @GetMapping("/users")
    public List<UserResponse> listUsers(@RequestParam(required = false) Role role, Authentication auth) {

        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = principal.getSchool().getId();

        List<User> users = (role == null)
                ? userRepo.findBySchool_Id(schoolId)
                : userRepo.findBySchool_IdAndRole(schoolId, role);

        return users.stream().map(UserResponse::from).toList();
    }

    // ✅ Enable/Disable user
    @PatchMapping("/users/{id}/enabled")
    public UserResponse setEnabled(@PathVariable Long id,
                                   @RequestParam boolean enabled,
                                   Authentication auth) {

        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = principal.getSchool().getId();

        User u = userRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!u.getSchool().getId().equals(schoolId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your school user");
        }

        // don't allow principal disabling themself
        if (u.getUsername().equals(principal.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot disable yourself");
        }

        u.setEnabled(enabled);
        return UserResponse.from(userRepo.save(u));
    }

    public record ResetPasswordRequest(String newPassword) {}

    @PutMapping("/users/{id}/password")
    public UserResponse resetPassword(@PathVariable Long id,
                                      @RequestBody ResetPasswordRequest body,
                                      Authentication auth) {

        String newPassword = (body == null) ? null : body.newPassword();

        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        User principal = userRepo.findByUsername(auth.getName()).orElseThrow();
        Long schoolId = principal.getSchool().getId();

        User u = userRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!u.getSchool().getId().equals(schoolId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your school user");
        }

        u.setPasswordHash(passwordEncoder.encode(newPassword));
        return UserResponse.from(userRepo.save(u));
    }

}
