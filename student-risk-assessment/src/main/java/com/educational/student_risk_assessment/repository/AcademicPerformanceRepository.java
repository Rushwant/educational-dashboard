package com.educational.student_risk_assessment.repository;

import com.educational.student_risk_assessment.entity.AcademicPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AcademicPerformanceRepository extends JpaRepository<AcademicPerformance, UUID> {

    List<AcademicPerformance> findByStudentIdAndSemester(UUID studentId, String semester);
}
