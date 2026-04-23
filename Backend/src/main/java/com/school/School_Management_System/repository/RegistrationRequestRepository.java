package com.school.School_Management_System.repository;

import com.school.School_Management_System.model.RegistrationRequest;
import com.school.School_Management_System.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {

    // Find all requests for a school with specific status (e.g., pending)
    List<RegistrationRequest> findBySchool_IdAndStatus(Long schoolId, RequestStatus status);

    // Find by email
    Optional<RegistrationRequest> findByEmail(String email);

    // Check if email exists (for validation)
    boolean existsByEmail(String email);
}