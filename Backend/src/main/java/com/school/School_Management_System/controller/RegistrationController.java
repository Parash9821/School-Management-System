package com.school.School_Management_System.controller;

import com.school.School_Management_System.dto.RegistrationRequestDTO;
import com.school.School_Management_System.model.RegistrationRequest;
import com.school.School_Management_System.model.Role;
import com.school.School_Management_System.model.School;
import com.school.School_Management_System.model.RequestStatus;
import com.school.School_Management_System.repository.RegistrationRequestRepository;
import com.school.School_Management_System.repository.SchoolRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final RegistrationRequestRepository requestRepo;
    private final SchoolRepository schoolRepo;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(RegistrationRequestRepository requestRepo,
                                  SchoolRepository schoolRepo,
                                  PasswordEncoder passwordEncoder) {
        this.requestRepo = requestRepo;
        this.schoolRepo = schoolRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String submitRequest(@Valid @RequestBody RegistrationRequestDTO dto) {

        // Check duplicate email
        if (requestRepo.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered or pending approval");
        }

        // Get school
        School school = schoolRepo.findById(dto.getSchoolId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid school ID"));

        // Create registration request
        RegistrationRequest req = new RegistrationRequest();
        req.setFullName(dto.getFullName());
        req.setEmail(dto.getEmail());
        req.setRole(dto.getRole());
        req.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        req.setSchool(school);
        req.setStatus(RequestStatus.PENDING);

        requestRepo.save(req);

        return "Registration submitted successfully, pending approval";
    }
}