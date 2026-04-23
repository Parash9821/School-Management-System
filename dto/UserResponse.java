package com.school.School_Management_System.dto;

import com.school.School_Management_System.model.Role;
import com.school.School_Management_System.model.User;

public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private boolean enabled;
    private Long schoolId;

    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.id = u.getId();
        r.username = u.getUsername();
        r.fullName = u.getFullName();
        r.role = u.getRole();
        r.enabled = u.isEnabled();
        r.schoolId = u.getSchool() != null ? u.getSchool().getId() : null;
        return r;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public Long getSchoolId() { return schoolId; }
}
