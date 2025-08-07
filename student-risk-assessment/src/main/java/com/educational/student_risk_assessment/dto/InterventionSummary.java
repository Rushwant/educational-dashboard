package com.educational.student_risk_assessment.dto;

import java.math.BigDecimal;

public class InterventionSummary {

    private String semester;
    private int totalInterventions;
    private int completedInterventions;
    private int onTrackInterventions;
    private int notOnTrackInterventions;
    private BigDecimal averageProgressRate;

    // Constructors
    public InterventionSummary() {}

    public InterventionSummary(String semester, int totalInterventions,
                               int completedInterventions, int onTrackInterventions,
                               int notOnTrackInterventions, BigDecimal averageProgressRate) {
        this.semester = semester;
        this.totalInterventions = totalInterventions;
        this.completedInterventions = completedInterventions;
        this.onTrackInterventions = onTrackInterventions;
        this.notOnTrackInterventions = notOnTrackInterventions;
        this.averageProgressRate = averageProgressRate;
    }

    // Getters and Setters
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getTotalInterventions() { return totalInterventions; }
    public void setTotalInterventions(int totalInterventions) { this.totalInterventions = totalInterventions; }

    public int getCompletedInterventions() { return completedInterventions; }
    public void setCompletedInterventions(int completedInterventions) { this.completedInterventions = completedInterventions; }

    public int getOnTrackInterventions() { return onTrackInterventions; }
    public void setOnTrackInterventions(int onTrackInterventions) { this.onTrackInterventions = onTrackInterventions; }

    public int getNotOnTrackInterventions() { return notOnTrackInterventions; }
    public void setNotOnTrackInterventions(int notOnTrackInterventions) { this.notOnTrackInterventions = notOnTrackInterventions; }

    public BigDecimal getAverageProgressRate() { return averageProgressRate; }
    public void setAverageProgressRate(BigDecimal averageProgressRate) { this.averageProgressRate = averageProgressRate; }
}
