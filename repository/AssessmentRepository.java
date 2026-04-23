package com.school.School_Management_System.repository;

import com.school.School_Management_System.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findBySchool_IdAndClassroom_Id(Long schoolId, Long classroomId);
    List<Assessment> findByClassroom_IdAndSubject_Id(Long classroomId, Long subjectId);
}
