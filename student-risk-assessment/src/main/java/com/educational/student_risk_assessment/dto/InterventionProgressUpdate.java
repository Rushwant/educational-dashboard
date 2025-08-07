package com.educational.student_risk_assessment.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InterventionProgressUpdate {
    @NotNull(message = "Current score is required.")
    @DecimalMin(value = "0.0", message = "Current score must be at least 0.")
    @DecimalMax(value = "100.0", message = "Current score must not exceed 100.")
    private BigDecimal currentScore;

    @NotNull(message = "Update date is required.")
    private LocalDate updatedOn;

    @Pattern(regexp = "ON_TRACK|NOT_ON_TRACK|COMPLETED", message = "Status must be ON_TRACK, NOT_ON_TRACK, or COMPLETED.")
    private String status;

    // Getters and setters
    public BigDecimal getCurrentScore() { return currentScore; }
    public void setCurrentScore(BigDecimal currentScore) { this.currentScore = currentScore; }

    public LocalDate getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDate updatedOn) { this.updatedOn = updatedOn; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
