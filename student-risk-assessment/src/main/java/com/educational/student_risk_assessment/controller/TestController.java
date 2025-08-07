package com.educational.student_risk_assessment.controller;

import com.educational.student_risk_assessment.dto.StudentRiskAssessment;
import com.educational.student_risk_assessment.entity.AcademicPerformance;
import com.educational.student_risk_assessment.entity.Attendance;
import com.educational.student_risk_assessment.entity.Behavior;
import com.educational.student_risk_assessment.entity.Student;
import com.educational.student_risk_assessment.repository.AcademicPerformanceRepository;
import com.educational.student_risk_assessment.repository.AttendanceRepository;
import com.educational.student_risk_assessment.repository.BehaviorRepository;
import com.educational.student_risk_assessment.repository.StudentRepository;
import com.educational.student_risk_assessment.service.RiskAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class TestController {
    @Autowired
    private RiskAssessmentService riskAssessmentService;

@Autowired
private StudentRepository studentRepository;

@Autowired
private AcademicPerformanceRepository academicPerformanceRepository;

@Autowired
private AttendanceRepository attendanceRepository;

@Autowired
private BehaviorRepository behaviorRepository;

    @GetMapping("/test-business-logic")
    public String testBusinessLogic() {
        try {
            StringBuilder results = new StringBuilder("✅ Business Logic Test Results:<br>");

            // === HIGH RISK STUDENT ===
            Student highRiskStudent = new Student("High Risk", "10th");
            studentRepository.save(highRiskStudent);

            AcademicPerformance highRiskAcademic = new AcademicPerformance();
            highRiskAcademic.setStudent(highRiskStudent);
            highRiskAcademic.setSemester("2024-Fall");
            highRiskAcademic.setCourse("Math");
            highRiskAcademic.setGrade(new BigDecimal("58"));
            highRiskAcademic.setStateAssessmentMath(450);
            academicPerformanceRepository.save(highRiskAcademic);

            Attendance highRiskAttendance = new Attendance();
            highRiskAttendance.setStudent(highRiskStudent);
            highRiskAttendance.setSemester("2024-Fall");
            highRiskAttendance.setAttendanceRate(new BigDecimal("80"));
            highRiskAttendance.setAbsentDays(15);
            highRiskAttendance.setTardyDays(9);
            attendanceRepository.save(highRiskAttendance);

            Behavior highRiskBehavior = new Behavior();
            highRiskBehavior.setStudent(highRiskStudent);
            highRiskBehavior.setSemester("2024-Fall");
            highRiskBehavior.setDisciplinaryActions(5);
            highRiskBehavior.setSuspensions(2);
            behaviorRepository.save(highRiskBehavior);

            results.append(String.format(
                    "High Risk Student: %s (ID: %s)<br>", highRiskStudent.getName(), highRiskStudent.getId()));


// === MEDIUM RISK STUDENT ===
            Student mediumRiskStudent = new Student("Medium Risk", "10th");
            studentRepository.save(mediumRiskStudent);


            AcademicPerformance medRiskAcademic = new AcademicPerformance();
            medRiskAcademic.setStudent(mediumRiskStudent);
            medRiskAcademic.setSemester("2024-Fall");
            medRiskAcademic.setCourse("Science");
            medRiskAcademic.setGrade(new BigDecimal("68"));
            medRiskAcademic.setStateAssessmentMath(510);
            academicPerformanceRepository.save(medRiskAcademic);


            Attendance medRiskAttendance = new Attendance();
            medRiskAttendance.setStudent(mediumRiskStudent);
            medRiskAttendance.setSemester("2024-Fall");
            medRiskAttendance.setAttendanceRate(new BigDecimal("88"));
            medRiskAttendance.setAbsentDays(4);
            medRiskAttendance.setTardyDays(2);
            attendanceRepository.save(medRiskAttendance);

// Behavior: no risk
            Behavior medRiskBehavior = new Behavior();
            medRiskBehavior.setStudent(mediumRiskStudent);
            medRiskBehavior.setSemester("2024-Fall");
            medRiskBehavior.setDisciplinaryActions(0);
            medRiskBehavior.setSuspensions(0);
            behaviorRepository.save(medRiskBehavior);

            results.append(String.format(
                    "Medium Risk Student: %s (ID: %s)<br>", mediumRiskStudent.getName(), mediumRiskStudent.getId()));


            // === LOW RISK STUDENT ===
            Student lowRiskStudent = new Student("Low Risk", "10th");
            studentRepository.save(lowRiskStudent);

            AcademicPerformance lowRiskAcademic = new AcademicPerformance();
            lowRiskAcademic.setStudent(lowRiskStudent);
            lowRiskAcademic.setSemester("2024-Fall");
            lowRiskAcademic.setCourse("English");
            lowRiskAcademic.setGrade(new BigDecimal("88"));
            lowRiskAcademic.setStateAssessmentMath(550);
            academicPerformanceRepository.save(lowRiskAcademic);

            Attendance lowRiskAttendance = new Attendance();
            lowRiskAttendance.setStudent(lowRiskStudent);
            lowRiskAttendance.setSemester("2024-Fall");
            lowRiskAttendance.setAttendanceRate(new BigDecimal("96"));
            lowRiskAttendance.setAbsentDays(2);
            lowRiskAttendance.setTardyDays(1);
            attendanceRepository.save(lowRiskAttendance);

            Behavior lowRiskBehavior = new Behavior();
            lowRiskBehavior.setStudent(lowRiskStudent);
            lowRiskBehavior.setSemester("2024-Fall");
            lowRiskBehavior.setDisciplinaryActions(0);
            lowRiskBehavior.setSuspensions(0);
            behaviorRepository.save(lowRiskBehavior);

            results.append(String.format(
                    "Low Risk Student: %s (ID: %s)<br>", lowRiskStudent.getName(), lowRiskStudent.getId()));


            results.append("<br>Use these IDs to test risk assessments and recommendations for HIGH, MEDIUM, and LOW cases.");
            return results.toString();

        } catch (Exception e) {
            return "❌ Business Logic Test Failed: " + e.getMessage();
        }
    }


    @GetMapping("/test-jwt")
    public String testJWT() {
        return "✅ JWT Authentication implemented! \n\n" +
                "To test:\n" +
                "1. POST /api/auth/login with {\"username\":\"teacher\", \"password\":\"password\"}\n" +
                "2. Use returned JWT token in Authorization header: 'Bearer <token>'\n" +
                "3. Access protected endpoints like /api/risk-assessment/at-risk";
    }

}
