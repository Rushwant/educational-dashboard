package com.educational.student_risk_assessment.service;

import com.educational.student_risk_assessment.dto.CreateInterventionRequest;
import com.educational.student_risk_assessment.dto.InterventionProgressUpdate;
import com.educational.student_risk_assessment.dto.InterventionSummary;
import com.educational.student_risk_assessment.entity.Intervention;
import com.educational.student_risk_assessment.entity.Student;
import com.educational.student_risk_assessment.repository.InterventionRepository;
import com.educational.student_risk_assessment.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class InterventionService {

    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private StudentRepository studentRepository;

    public Intervention createIntervention(CreateInterventionRequest request) {
        // Validate student exists
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + request.getStudentId()));

        // Validate dates
        if (request.getTargetCompletionDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("Target completion date cannot be before start date");
        }

        // Create intervention
        Intervention intervention = new Intervention();
        intervention.setStudent(student);
        intervention.setInterventionType(request.getInterventionType());
        intervention.setStartDate(request.getStartDate());
        intervention.setTargetCompletionDate(request.getTargetCompletionDate());
        intervention.setStartScore(request.getStartScore());
        intervention.setCurrentScore(request.getStartScore()); // Initially same as start score
        intervention.setGoalScore(request.getGoalScore());
        intervention.setStatus("ON_TRACK"); // Default status

        return interventionRepository.save(intervention);
    }

    public Intervention updateInterventionProgress(String interventionId,
                                                   InterventionProgressUpdate update) {
        UUID interventionUuid = UUID.fromString(interventionId);
        Intervention intervention = interventionRepository.findById(interventionUuid)
                .orElseThrow(() -> new RuntimeException("Intervention not found with ID: " + interventionId));

        // Update current score
        intervention.setCurrentScore(update.getCurrentScore());

        // Determine if student is on track
        boolean onTrack = isStudentOnTrack(interventionId);
        intervention.setStatus(onTrack ? "ON_TRACK" : "NOT_ON_TRACK");

        // Check if goal is met
        if (update.getCurrentScore().compareTo(intervention.getGoalScore()) >= 0) {
            intervention.setStatus("COMPLETED");
        }

        return interventionRepository.save(intervention);
    }

    public List<Intervention> getStudentInterventions(String studentId) {
        UUID studentUuid = UUID.fromString(studentId);

        // Validate student exists
        studentRepository.findById(studentUuid)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        return interventionRepository.findByStudentId(studentUuid);
    }

    public InterventionSummary getInterventionSummary(String semester) {
        int year;

        try {
            year = Integer.parseInt(semester); // Assuming semester is like "2024"
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid semester format. Expected year format like '2024'");
        }

        List<Intervention> interventions = interventionRepository.findBySemester(year);

        int totalInterventions = interventions.size();
        int completedInterventions = 0;
        int onTrackInterventions = 0;
        int notOnTrackInterventions = 0;
        BigDecimal totalProgressRate = BigDecimal.ZERO;

        for (Intervention intervention : interventions) {
            switch (intervention.getStatus()) {
                case "COMPLETED":
                    completedInterventions++;
                    break;
                case "ON_TRACK":
                    onTrackInterventions++;
                    break;
                case "NOT_ON_TRACK":
                    notOnTrackInterventions++;
                    break;
            }

            // Calculate progress rate for this intervention
            BigDecimal progressRate = calculateProgressRate(intervention);
            totalProgressRate = totalProgressRate.add(progressRate);
        }

        // Calculate average progress rate
        BigDecimal averageProgressRate = totalInterventions > 0 ?
                totalProgressRate.divide(new BigDecimal(totalInterventions), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return new InterventionSummary(
                semester,
                totalInterventions,
                completedInterventions,
                onTrackInterventions,
                notOnTrackInterventions,
                averageProgressRate
        );
    }

    public boolean isStudentOnTrack(String interventionId) {
        UUID interventionUUID = UUID.fromString(interventionId);

        Intervention intervention = interventionRepository.findById(interventionUUID)
                .orElseThrow(() -> new RuntimeException("Intervention not found with ID: " + interventionId));

        LocalDate startDate = intervention.getStartDate();
        LocalDate targetDate = intervention.getTargetCompletionDate();
        LocalDate currentDate = LocalDate.now();

        // Calculate time progress (how much of the timeline has elapsed)
        long totalDays = ChronoUnit.DAYS.between(startDate, targetDate);
        long elapsedDays = ChronoUnit.DAYS.between(startDate, currentDate);

        // Prevent division by zero
        if (totalDays <= 0) {
            return false;
        }

        double timeProgress = (double) elapsedDays / totalDays;

        // Calculate score progress (how much improvement has been made)
        BigDecimal startScore = intervention.getStartScore();
        BigDecimal currentScore = intervention.getCurrentScore();
        BigDecimal goalScore = intervention.getGoalScore();

        BigDecimal totalNeededImprovement = startScore.subtract(goalScore);
        BigDecimal actualImprovement = startScore.subtract(currentScore);

        // Prevent division by zero
        if (totalNeededImprovement.compareTo(BigDecimal.ZERO) <= 0) {
            return true; // Already at or past goal
        }

        double scoreProgress = actualImprovement.divide(totalNeededImprovement, 4, RoundingMode.HALF_UP).doubleValue();

        // Student is on track if their score progress is at least 80% of their time progress
        return scoreProgress >= (timeProgress * 0.8);
    }


    private BigDecimal calculateProgressRate(Intervention intervention) {
        BigDecimal scoreRange = intervention.getGoalScore().subtract(intervention.getStartScore());

        if (scoreRange.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.valueOf(100); // Already at or past goal
        }

        BigDecimal actualProgress = intervention.getCurrentScore().subtract(intervention.getStartScore());

        return actualProgress.divide(scoreRange, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
