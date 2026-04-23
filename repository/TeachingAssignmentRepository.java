package com.school.School_Management_System.repository;

import com.school.School_Management_System.model.Enrollment;
import com.school.School_Management_System.model.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {

    Optional<TeachingAssignment> findBySchool_IdAndClassroom_IdAndSubject_Id(Long schoolId, Long classroomId, Long subjectId);

    boolean existsBySchool_IdAndClassroom_IdAndSubject_Id(Long schoolId, Long classroomId, Long subjectId);

    List<TeachingAssignment> findBySchool_IdAndClassroom_Id(Long schoolId, Long classroomId);
    List<TeachingAssignment> findBySchool_IdAndTeacher_Id(Long schoolId, Long teacherId);
    List<TeachingAssignment> findByTeacher_Id(Long teacherId);

    boolean existsBySchool_IdAndClassroom_IdAndTeacher_Id(Long schoolId, Long classroomId, Long teacherId);


}
