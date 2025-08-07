package com.educational.student_risk_assessment.repository;

import com.educational.student_risk_assessment.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    Optional<Attendance> findByStudentIdAndSemester(UUID studentId, String semester);
}
