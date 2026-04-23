package com.school.School_Management_System.dto;

import com.school.School_Management_System.model.RequestStatus;
import com.school.School_Management_System.model.Role;

public record RegistrationRequestResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        RequestStatus status,
        Long schoolId,
        String schoolName
) {

    public static RegistrationRequestResponse fromEntity(
            com.school.School_Management_System.model.RegistrationRequest req
    ) {
        return new RegistrationRequestResponse(
                req.getId(),
                req.getFullName(),
                req.getEmail(),
                req.getRole(),
                req.getStatus(),
                req.getSchool().getId(),
                req.getSchool().getName()
        );
    }
}