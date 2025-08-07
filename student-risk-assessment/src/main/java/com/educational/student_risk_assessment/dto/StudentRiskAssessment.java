package com.educational.student_risk_assessment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class StudentRiskAssessment {

    private UUID studentId;
    private String studentName;
    private String semester;
    private BigDecimal totalRiskScore;
    private String riskLevel; // HIGH, MEDIUM, LOW

    // Score breakdown
    private BigDecimal academicScore;
    private BigDecimal attendanceScore;
    private BigDecimal behaviorScore;
    private BigDecimal tardinessScore;

    // Constructors
    public StudentRiskAssessment() {}

    public StudentRiskAssessment(UUID studentId, String studentName, String semester,
                                 BigDecimal totalRiskScore, String riskLevel) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.semester = semester;
        this.totalRiskScore = totalRiskScore;
        this.riskLevel = riskLevel;
    }

    // Getters and Setters
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public BigDecimal getTotalRiskScore() { return totalRiskScore; }
    public void setTotalRiskScore(BigDecimal totalRiskScore) { this.totalRiskScore = totalRiskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public BigDecimal getAcademicScore() { return academicScore; }
    public void setAcademicScore(BigDecimal academicScore) { this.academicScore = academicScore; }

    public BigDecimal getAttendanceScore() { return attendanceScore; }
    public void setAttendanceScore(BigDecimal attendanceScore) { this.attendanceScore = attendanceScore; }

    public BigDecimal getBehaviorScore() { return behaviorScore; }
    public void setBehaviorScore(BigDecimal behaviorScore) { this.behaviorScore = behaviorScore; }

    public BigDecimal getTardinessScore() { return tardinessScore; }
    public void setTardinessScore(BigDecimal tardinessScore) { this.tardinessScore = tardinessScore; }
}
