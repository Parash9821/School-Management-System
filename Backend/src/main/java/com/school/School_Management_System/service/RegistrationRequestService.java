package com.school.School_Management_System.service;

import com.school.School_Management_System.model.RequestStatus;
import com.school.School_Management_System.model.RegistrationRequest;
import com.school.School_Management_System.model.User;
import com.school.School_Management_System.repository.RegistrationRequestRepository;
import com.school.School_Management_System.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class RegistrationRequestService {

    private final RegistrationRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public RegistrationRequestService(
            RegistrationRequestRepository requestRepo,
            UserRepository userRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.requestRepo = requestRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // Get all requests
    public List<RegistrationRequest> getAllRequests() {
        return requestRepo.findAll();
    }

    // Approve a request and create User
    public RegistrationRequest approveRequest(Long requestId) {
        RegistrationRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request already processed");
        }

        // Use email as username (or generate differently if needed)
        String username = req.getEmail().split("@")[0];

        // Check duplicates
        if (userRepo.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists: " + username);
        }
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists: " + req.getEmail());
        }

        // Create User
        User newUser = new User();
        newUser.setFullName(req.getFullName());
        newUser.setUsername(username);               // username from email
        newUser.setEmail(req.getEmail());
        newUser.setRole(req.getRole());
        newUser.setEnabled(true);
        newUser.setSchool(req.getSchool());          // required by User entity
        newUser.setPasswordHash(passwordEncoder.encode(req.getPasswordHash())); // hash the password

        // Save user
        userRepo.save(newUser);

        // Update request status
        req.setStatus(RequestStatus.ACCEPTED);
        return requestRepo.save(req);
    }

    // Reject a request
    public RegistrationRequest rejectRequest(Long requestId) {
        RegistrationRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request already processed");
        }

        req.setStatus(RequestStatus.REJECTED);
        return requestRepo.save(req);
    }
}