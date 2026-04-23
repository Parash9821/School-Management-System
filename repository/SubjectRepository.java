package com.school.School_Management_System.repository;

import com.school.School_Management_System.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findBySchool_Id(Long schoolId);
    List<Subject> findBySchool_IdOrderByIdAsc(Long schoolId);
    boolean existsBySchool_IdAndCode(Long schoolId, String code);
}
