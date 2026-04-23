package com.school.School_Management_System.controller;

import com.school.School_Management_System.dto.RegistrationRequestResponse;
import com.school.School_Management_System.model.*;
import com.school.School_Management_System.repository.RegistrationRequestRepository;
import com.school.School_Management_System.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/principal/requests")
public class PrincipalRegistrationController {

    private final RegistrationRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public PrincipalRegistrationController(
            RegistrationRequestRepository requestRepo,
            UserRepository userRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.requestRepo = requestRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ List pending requests for Principal's school
    @GetMapping
    public List<RegistrationRequestResponse> listPendingRequests(Authentication auth) {
        User principal = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Long schoolId = principal.getSchool().getId();

        return requestRepo.findBySchool_IdAndStatus(schoolId, RequestStatus.PENDING)
                .stream()
                .map(RegistrationRequestResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ Approve a registration request
    @PostMapping("/{id}/approve")
    public String approveRequest(@PathVariable Long id, Authentication auth) {
        User principal = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        RegistrationRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!req.getSchool().getId().equals(principal.getSchool().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your school request");
        }

        // Check if user with same email exists
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists");
        }

        // Create user
        User u = new User();
        u.setSchool(req.getSchool());
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setUsername(generateUsername(req.getFullName())); // simple username generator
        u.setRole(req.getRole());
        u.setEnabled(true);
        u.setPasswordHash(req.getPasswordHash());

        userRepo.save(u);

        // Update request status
        req.setStatus(RequestStatus.ACCEPTED);
        requestRepo.save(req);

        return "Registration request approved ✅";
    }

    // ✅ Reject a registration request
    @PostMapping("/{id}/reject")
    public String rejectRequest(@PathVariable Long id, Authentication auth) {
        User principal = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        RegistrationRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!req.getSchool().getId().equals(principal.getSchool().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your school request");
        }

        req.setStatus(RequestStatus.REJECTED);
        requestRepo.save(req);

        return "Registration request rejected ❌";
    }

    // ====================== HELPER ======================
    private String generateUsername(String fullName) {
        // simple username: first part + random digits
        String base = fullName.trim().toLowerCase().split(" ")[0];
        int rand = (int) (Math.random() * 1000);
        return base + rand;
    }
}