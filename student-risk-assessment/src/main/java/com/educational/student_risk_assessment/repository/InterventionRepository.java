package com.educational.student_risk_assessment.repository;

import com.educational.student_risk_assessment.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, UUID> {

    List<Intervention> findByStudentId(UUID studentId);

    @Query("SELECT i FROM Intervention i WHERE YEAR(i.startDate) = :year")
    List<Intervention> findBySemester(@Param("year") Integer year);

    List<Intervention> findByStatus(String status);
}
