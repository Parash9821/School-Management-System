package com.school.School_Management_System.repository;

import com.school.School_Management_System.model.ResultPublish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ResultPublicationRepository extends JpaRepository<ResultPublish, Long> {

    Optional<ResultPublish> findBySchool_IdAndClassroom_IdAndAcademicYear(
            Long schoolId,
            Long classroomId,
            String academicYear
    );

    // For scheduler: publishAt <= now and not yet published
    List<ResultPublish> findByPublishedFalseAndPublishAtNotNullAndPublishAtLessThanEqual(
            LocalDateTime now
    );
}
