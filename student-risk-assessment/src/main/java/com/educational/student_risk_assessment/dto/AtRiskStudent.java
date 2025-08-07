package com.educational.student_risk_assessment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class AtRiskStudent {

    private UUID studentId;
    private String studentName;
    private String grade;
    private BigDecimal riskScore;
    private String riskLevel;
    private String semester;

    // Constructors
    public AtRiskStudent() {}

    public AtRiskStudent(UUID studentId, String studentName, String grade,
                         BigDecimal riskScore, String riskLevel, String semester) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.grade = grade;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.semester = semester;
    }

    // Getters and Setters
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}
