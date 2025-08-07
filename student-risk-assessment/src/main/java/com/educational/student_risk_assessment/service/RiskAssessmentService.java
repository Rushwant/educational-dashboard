package com.educational.student_risk_assessment.service;
import com.educational.student_risk_assessment.dto.InterventionRecommendation;

import com.educational.student_risk_assessment.dto.AtRiskStudent;
import com.educational.student_risk_assessment.dto.RiskLevel;
import com.educational.student_risk_assessment.dto.StudentRiskAssessment;
import com.educational.student_risk_assessment.entity.*;
import com.educational.student_risk_assessment.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RiskAssessmentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AcademicPerformanceRepository academicPerformanceRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private BehaviorRepository behaviorRepository;

    public StudentRiskAssessment calculateRiskScore(String studentId, String semester) {
        UUID studentUuid = UUID.fromString(studentId);
        Student student = studentRepository.findById(studentUuid)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Calculate individual risk components
        BigDecimal academicScore = calculateAcademicRisk(studentUuid, semester);
        BigDecimal attendanceScore = calculateAttendanceRisk(studentUuid, semester);
        BigDecimal behaviorScore = calculateBehaviorRisk(studentUuid, semester);
        BigDecimal tardinessScore = calculateTardinessRisk(studentUuid, semester);

        // Calculate total risk score
        BigDecimal totalScore = academicScore.add(attendanceScore)
                .add(behaviorScore)
                .add(tardinessScore);

        // Determine risk level
        String riskLevel = determineRiskLevel(totalScore);

        // Build result
        StudentRiskAssessment assessment = new StudentRiskAssessment(
                studentUuid, student.getName(), semester, totalScore, riskLevel
        );
        assessment.setAcademicScore(academicScore);
        assessment.setAttendanceScore(attendanceScore);
        assessment.setBehaviorScore(behaviorScore);
        assessment.setTardinessScore(tardinessScore);

        return assessment;
    }

    private BigDecimal calculateAcademicRisk(UUID studentId, String semester) {
        List<AcademicPerformance> performances = academicPerformanceRepository
                .findByStudentIdAndSemester(studentId, semester);

        BigDecimal score = BigDecimal.ZERO;

        for (AcademicPerformance performance : performances) {
            // Course grades below 70% = 25 points
            if (performance.getGrade() != null &&
                    performance.getGrade().compareTo(new BigDecimal("70")) < 0) {
                score = score.add(new BigDecimal("25"));
            }

            // State assessment scores below 500 = 15 points
            if (performance.getStateAssessmentEla() != null &&
                    performance.getStateAssessmentEla() < 500) {
                score = score.add(new BigDecimal("15"));
            }

            if (performance.getStateAssessmentMath() != null &&
                    performance.getStateAssessmentMath() < 500) {
                score = score.add(new BigDecimal("15"));
            }
        }

        // Cap at 40 points (40% weight)
        return score.min(new BigDecimal("40"));
    }

    private BigDecimal calculateAttendanceRisk(UUID studentId, String semester) {
        return attendanceRepository.findByStudentIdAndSemester(studentId, semester)
                .map(attendance -> {
                    BigDecimal score = BigDecimal.ZERO;

                    // Attendance rate < 90% = 20 points
                    if (attendance.getAttendanceRate() != null &&
                            attendance.getAttendanceRate().compareTo(new BigDecimal("90")) < 0) {
                        score = score.add(new BigDecimal("20"));
                    }

                    // Absent days > 10 = 10 points
                    if (attendance.getAbsentDays() != null && attendance.getAbsentDays() > 10) {
                        score = score.add(new BigDecimal("10"));
                    }

                    return score.min(new BigDecimal("30")); // Cap at 30 points (30% weight)
                })
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateBehaviorRisk(UUID studentId, String semester) {
        return behaviorRepository.findByStudentIdAndSemester(studentId, semester)
                .map(behavior -> {
                    BigDecimal score = BigDecimal.ZERO;

                    // Disciplinary actions > 2 = 15 points
                    if (behavior.getDisciplinaryActions() != null &&
                            behavior.getDisciplinaryActions() > 2) {
                        score = score.add(new BigDecimal("15"));
                    }

                    // Suspensions > 0 = 5 points
                    if (behavior.getSuspensions() != null && behavior.getSuspensions() > 0) {
                        score = score.add(new BigDecimal("5"));
                    }

                    return score.min(new BigDecimal("20")); // Cap at 20 points (20% weight)
                })
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateTardinessRisk(UUID studentId, String semester) {
        return attendanceRepository.findByStudentIdAndSemester(studentId, semester)
                .map(attendance -> {
                    BigDecimal score = BigDecimal.ZERO;

                    // Tardy days > 5 = 10 points
                    if (attendance.getTardyDays() != null && attendance.getTardyDays() > 5) {
                        score = score.add(new BigDecimal("10"));
                    }

                    return score.min(new BigDecimal("10")); // Cap at 10 points (10% weight)
                })
                .orElse(BigDecimal.ZERO);
    }

    private String determineRiskLevel(BigDecimal totalScore) {
        if (totalScore.compareTo(new BigDecimal("70")) >= 0) {
            return RiskLevel.HIGH.name();
        } else if (totalScore.compareTo(new BigDecimal("40")) >= 0) {
            return RiskLevel.MEDIUM.name();
        } else {
            return RiskLevel.LOW.name();
        }
    }

    public List<AtRiskStudent> identifyAtRiskStudents(String semester, RiskLevel minimumRisk) {
        List<Student> allStudents = studentRepository.findAll();
        List<AtRiskStudent> atRiskStudents = new ArrayList<>();

        for (Student student : allStudents) {
            StudentRiskAssessment assessment = calculateRiskScore(
                    student.getId().toString(), semester
            );

            RiskLevel studentRiskLevel = RiskLevel.valueOf(assessment.getRiskLevel());

            // Check if student meets minimum risk threshold
            if (shouldIncludeStudent(studentRiskLevel, minimumRisk)) {
                atRiskStudents.add(new AtRiskStudent(
                        student.getId(),
                        student.getName(),
                        student.getGrade(),
                        assessment.getTotalRiskScore(),
                        assessment.getRiskLevel(),
                        semester
                ));
            }
        }

        return atRiskStudents;
    }

    private boolean shouldIncludeStudent(RiskLevel studentLevel, RiskLevel minimumLevel) {
        if (minimumLevel == null) return true;

        // HIGH > MEDIUM > LOW
        int studentPriority = getRiskPriority(studentLevel);
        int minimumPriority = getRiskPriority(minimumLevel);

        return studentPriority >= minimumPriority;
    }

    private int getRiskPriority(RiskLevel level) {
        switch (level) {
            case HIGH: return 3;
            case MEDIUM: return 2;
            case LOW: return 1;
            default: return 0;
        }
    }

    public void notifyStakeholders(List<AtRiskStudent> atRiskStudents) {
        // Placeholder implementation for notification logic
        // In a real system, this would send emails, create alerts, etc.
        System.out.println("Notifying stakeholders about " + atRiskStudents.size() + " at-risk students");

        for (AtRiskStudent student : atRiskStudents) {
            System.out.println("ALERT: Student " + student.getStudentName() +
                    " is at " + student.getRiskLevel() + " risk (Score: " +
                    student.getRiskScore() + ")");
        }
    }


    public List<InterventionRecommendation> recommendInterventionsForStudent(String studentId, String semester) {
        // Calculate the student's risk breakdown using your scoring logic
        StudentRiskAssessment assessment = calculateRiskScore(studentId, semester);

        List<InterventionRecommendation> recommendations = new ArrayList<>();

        // Standard recommendations based on risk level
        if ("HIGH".equals(assessment.getRiskLevel())) {
            recommendations.add(new InterventionRecommendation(
                    "Intensive Academic Support",
                    "Enroll student in tutoring and after-school remedial programs."
            ));
            recommendations.add(new InterventionRecommendation(
                    "Parental Engagement",
                    "Arrange urgent meeting with parents or guardians."
            ));
            recommendations.add(new InterventionRecommendation(
                    "Social Worker Referral",
                    "Consider referral to school social worker for additional support."
            ));
        } else if ("MEDIUM".equals(assessment.getRiskLevel())) {
            recommendations.add(new InterventionRecommendation(
                    "Regular Teacher Check-ins",
                    "Schedule weekly progress reviews with the student."
            ));
            recommendations.add(new InterventionRecommendation(
                    "Attendance Monitoring",
                    "Send attendance report to parents monthly."
            ));
        } else if ("LOW".equals(assessment.getRiskLevel())) {
            recommendations.add(new InterventionRecommendation(
                    "Standard Classroom Support",
                    "Maintain current supports; monitor quarterly."
            ));
        } else {
            System.out.println("DEBUG: Unexpected risk level: " + assessment.getRiskLevel());
        }

        // Additional targeted suggestions based on risk component breakdowns
        if (assessment.getAcademicScore().compareTo(new java.math.BigDecimal("20")) >= 0) {
            recommendations.add(new InterventionRecommendation(
                    "Subject-Specific Tutoring",
                    "Provide extra help in subjects where grades are low."
            ));
        }
        if (assessment.getAttendanceScore().compareTo(new java.math.BigDecimal("10")) >= 0) {
            recommendations.add(new InterventionRecommendation(
                    "Attendance Counseling",
                    "Schedule session to address absenteeism."
            ));
        }
        if (assessment.getTardinessScore().compareTo(new java.math.BigDecimal("5")) >= 0) {
            recommendations.add(new InterventionRecommendation(
                    "Time Management Workshop",
                    "Encourage student participation in time-management programs."
            ));
        }
        if (assessment.getBehaviorScore().compareTo(new java.math.BigDecimal("10")) >= 0) {
            recommendations.add(new InterventionRecommendation(
                    "Behavioral Coaching",
                    "Enroll student in behavioral improvement programs."
            ));
        }

        return recommendations;
    }

}
