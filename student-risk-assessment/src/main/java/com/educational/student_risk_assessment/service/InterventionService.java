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

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + request.getStudentId()));


        if (request.getTargetCompletionDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("Target completion date cannot be before start date");
        }


        Intervention intervention = new Intervention();
        intervention.setStudent(student);
        intervention.setInterventionType(request.getInterventionType());
        intervention.setStartDate(request.getStartDate());
        intervention.setTargetCompletionDate(request.getTargetCompletionDate());
        intervention.setStartScore(request.getStartScore());
        intervention.setCurrentScore(request.getStartScore());
        intervention.setGoalScore(request.getGoalScore());
        intervention.setStatus("ON_TRACK");

        return interventionRepository.save(intervention);
    }

    public Intervention updateInterventionProgress(String interventionId,
                                                   InterventionProgressUpdate update) {
        UUID interventionUuid = UUID.fromString(interventionId);
        Intervention intervention = interventionRepository.findById(interventionUuid)
                .orElseThrow(() -> new RuntimeException("Intervention not found with ID: " + interventionId));

        intervention.setCurrentScore(update.getCurrentScore());


        boolean onTrack = isStudentOnTrack(interventionId);
        intervention.setStatus(onTrack ? "ON_TRACK" : "NOT_ON_TRACK");


        if (update.getCurrentScore().compareTo(intervention.getGoalScore()) >= 0) {
            intervention.setStatus("COMPLETED");
        }

        return interventionRepository.save(intervention);
    }

    public List<Intervention> getStudentInterventions(String studentId) {
        UUID studentUuid = UUID.fromString(studentId);


        studentRepository.findById(studentUuid)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        return interventionRepository.findByStudentId(studentUuid);
    }

    public InterventionSummary getInterventionSummary(String semester) {
        int year;

        try {
            year = Integer.parseInt(semester);
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


            BigDecimal progressRate = calculateProgressRate(intervention);
            totalProgressRate = totalProgressRate.add(progressRate);
        }


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


        long totalDays = ChronoUnit.DAYS.between(startDate, targetDate);
        long elapsedDays = ChronoUnit.DAYS.between(startDate, currentDate);


        if (totalDays <= 0) {
            return false;
        }

        double timeProgress = (double) elapsedDays / totalDays;

        BigDecimal startScore = intervention.getStartScore();
        BigDecimal currentScore = intervention.getCurrentScore();
        BigDecimal goalScore = intervention.getGoalScore();

        BigDecimal totalNeededImprovement = startScore.subtract(goalScore);
        BigDecimal actualImprovement = startScore.subtract(currentScore);


        if (totalNeededImprovement.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }

        double scoreProgress = actualImprovement.divide(totalNeededImprovement, 4, RoundingMode.HALF_UP).doubleValue();


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
