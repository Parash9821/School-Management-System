package com.school.School_Management_System.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/principal")
public class PrincipalController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Principal dashboard ✅";
    }
}
