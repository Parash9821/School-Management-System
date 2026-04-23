package com.school.School_Management_System.repository;

import com.school.School_Management_System.model.Role;
import com.school.School_Management_System.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findBySchool_IdAndUsername(Long schoolId, String username);
    boolean existsBySchool_IdAndUsername(Long schoolId, String username);
    List<User> findBySchool_Id(Long schoolId);
    List<User> findBySchool_IdAndRole(Long schoolId, Role role);


    boolean existsByUsername(String username);
}
