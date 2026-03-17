package com.school.School_Management_System.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSecureController {

    @GetMapping("/api/secure")
    public String secure() {
        return "JWT authentication successful 🔐";
    }
}
