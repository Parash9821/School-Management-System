package com.school.School_Management_System.repository;

import com.school.School_Management_System.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findBySchool_Id(Long schoolId);
    List<Classroom> findBySchool_IdOrderByIdAsc(Long schoolId);


}
