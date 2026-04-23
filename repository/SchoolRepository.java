package com.school.School_Management_System.repository;

import com.school.School_Management_System.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findBySlug(String slug);
}
