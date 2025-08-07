package com.educational.student_risk_assessment.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CreateInterventionRequest {
    @NotNull(message = "Student ID is required.")
    private UUID studentId;

    @NotBlank(message = "Intervention type is required.")
    private String interventionType;

    @NotNull(message = "Start date is required.")
    private LocalDate startDate;

    @NotNull(message = "Target completion date is required.")
    private LocalDate targetCompletionDate;

    @NotNull(message = "Start score is required.")
    @DecimalMin(value = "0.0", message = "Start score must be at least 0.")
    @DecimalMax(value = "100.0", message = "Start score must not exceed 100.")
    private BigDecimal startScore;

    @NotNull(message = "Goal score is required.")
    @DecimalMin(value = "0.0", message = "Goal score must be at least 0.")
    @DecimalMax(value = "100.0", message = "Goal score must not exceed 100.")
    private BigDecimal goalScore;

    @DecimalMin(value = "0.0", message = "Current score must be at least 0.")
    @DecimalMax(value = "100.0", message = "Current score must not exceed 100.")
    private BigDecimal currentScore;

    // Getters and setters
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public String getInterventionType() { return interventionType; }
    public void setInterventionType(String interventionType) { this.interventionType = interventionType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getTargetCompletionDate() { return targetCompletionDate; }
    public void setTargetCompletionDate(LocalDate targetCompletionDate) { this.targetCompletionDate = targetCompletionDate; }

    public BigDecimal getStartScore() { return startScore; }
    public void setStartScore(BigDecimal startScore) { this.startScore = startScore; }

    public BigDecimal getGoalScore() { return goalScore; }
    public void setGoalScore(BigDecimal goalScore) { this.goalScore = goalScore; }

    public BigDecimal getCurrentScore() { return currentScore; }
    public void setCurrentScore(BigDecimal currentScore) { this.currentScore = currentScore; }
}
