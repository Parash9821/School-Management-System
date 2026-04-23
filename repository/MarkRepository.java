package com.school.School_Management_System.repository;

import com.school.School_Management_System.model.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarkRepository extends JpaRepository<Mark, Long> {

    // ===============================
    // Used to prevent duplicate marks
    // (Assessment + Student is UNIQUE)
    // ===============================
    Optional<Mark> findByAssessment_IdAndStudent_Id(Long assessmentId, Long studentId);

    // ===============================
    // Teacher analytics
    // ===============================
    @Query("""
        SELECT m 
        FROM Mark m 
        WHERE m.assessment.classroom.id = :classroomId
    """)
    List<Mark> findByClassroomId(@Param("classroomId") Long classroomId);

    @Query("""
        SELECT m 
        FROM Mark m 
        WHERE m.assessment.classroom.id = :classroomId
          AND m.assessment.subject.id = :subjectId
    """)
    List<Mark> findByClassroomIdAndSubjectId(@Param("classroomId") Long classroomId,
                                             @Param("subjectId") Long subjectId);

    // ===============================
    // Student views
    // ===============================
    @Query("""
        SELECT m 
        FROM Mark m 
        WHERE m.student.id = :studentId
        ORDER BY m.assessment.heldOn DESC
    """)
    List<Mark> findByStudentId(@Param("studentId") Long studentId);
    List<Mark> findByStudent_Id(Long studentId);

}
