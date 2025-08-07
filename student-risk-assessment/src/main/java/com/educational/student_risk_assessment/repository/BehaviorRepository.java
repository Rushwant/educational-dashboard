package com.educational.student_risk_assessment.repository;

import com.educational.student_risk_assessment.entity.Behavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BehaviorRepository extends JpaRepository<Behavior, UUID> {

    Optional<Behavior> findByStudentIdAndSemester(UUID studentId, String semester);
}
