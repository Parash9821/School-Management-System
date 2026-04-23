package com.school.School_Management_System.repository;

import com.school.School_Management_System.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findBySchool_IdAndStudent_Id(Long schoolId, Long studentUserId);

    boolean existsByStudent_IdAndClassroom_IdAndAcademicYear(Long studentId, Long classroomId, String academicYear);

    boolean existsBySchool_IdAndStudent_IdAndClassroom_IdAndAcademicYear(
            Long schoolId, Long studentId, Long classroomId, String academicYear
    );

    List<Enrollment> findByClassroom_IdAndAcademicYear(Long classroomId, String academicYear);

    // ✅ safer for principal listing
    List<Enrollment> findBySchool_IdAndClassroom_IdAndAcademicYear(Long schoolId, Long classroomId, String academicYear);

    List<Enrollment> findBySchool_IdAndClassroom_Id(Long schoolId, Long classroomId);
    List<Enrollment> findByClassroom_Id(Long classroomId);

    @Query("select e.student.id from Enrollment e where e.classroom.id = :classroomId")
    List<Long> findStudentIdsByClassroom(@Param("classroomId") Long classroomId);
    Optional<Enrollment> findBySchool_IdAndStudent_IdAndAcademicYear(Long schoolId, Long studentId, String academicYear);

    // ✅ safer for analytics (school + classroom + year)
    @Query("""
        select e.student.id
        from Enrollment e
        where e.school.id = :schoolId
          and e.classroom.id = :classroomId
          and e.academicYear = :academicYear
          and e.student.role = com.school.School_Management_System.model.Role.STUDENT
    """)
    List<Long> findStudentIdsBySchoolClassroomAndYear(@Param("schoolId") Long schoolId,
                                                      @Param("classroomId") Long classroomId,
                                                      @Param("academicYear") String academicYear);

    // ✅ keep old method (optional) if used elsewhere
    @Query("""
        select e.student.id
        from Enrollment e
        where e.classroom.id = :classroomId
          and e.academicYear = :academicYear
          and e.student.role = com.school.School_Management_System.model.Role.STUDENT
    """)
    List<Long> findStudentIdsByClassroomAndYear(@Param("classroomId") Long classroomId,
                                                @Param("academicYear") String academicYear);
}
