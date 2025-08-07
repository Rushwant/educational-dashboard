package com.educational.student_risk_assessment;

import com.educational.student_risk_assessment.dto.CreateInterventionRequest;
import com.educational.student_risk_assessment.dto.InterventionProgressUpdate;
import com.educational.student_risk_assessment.entity.Intervention;
import com.educational.student_risk_assessment.entity.Student;
import com.educational.student_risk_assessment.repository.InterventionRepository;
import com.educational.student_risk_assessment.repository.StudentRepository;
import com.educational.student_risk_assessment.service.InterventionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterventionServiceTest {

    @Mock
    private InterventionRepository interventionRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private InterventionService interventionService;

    private Student testStudent;
    private UUID studentId;
    private UUID interventionId;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        interventionId = UUID.randomUUID();

        testStudent = new Student();
        testStudent.setId(studentId);
        testStudent.setName("Test Student");
        testStudent.setGrade("10th");
    }

    @Test
    void createIntervention_ValidRequest_ReturnsIntervention() {
        // Arrange
        CreateInterventionRequest request = new CreateInterventionRequest();
        request.setStudentId(studentId);
        request.setInterventionType("Academic Support");
        request.setStartDate(LocalDate.now());
        request.setTargetCompletionDate(LocalDate.now().plusMonths(3));
        request.setStartScore(new BigDecimal("75"));
        request.setGoalScore(new BigDecimal("45"));

        Intervention savedIntervention = new Intervention();
        savedIntervention.setId(interventionId);
        savedIntervention.setStudent(testStudent);
        savedIntervention.setInterventionType("Academic Support");
        savedIntervention.setStartScore(new BigDecimal("75"));
        savedIntervention.setCurrentScore(new BigDecimal("75"));
        savedIntervention.setGoalScore(new BigDecimal("45"));
        savedIntervention.setStatus("NOT_ON_TRACK");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(interventionRepository.save(any(Intervention.class))).thenReturn(savedIntervention);

        // Act
        Intervention result = interventionService.createIntervention(request);

        // Assert
        assertNotNull(result);
        assertEquals(interventionId, result.getId());
        assertEquals("Academic Support", result.getInterventionType());
        assertEquals(testStudent, result.getStudent());
        assertEquals(new BigDecimal("75"), result.getStartScore());
        assertEquals(new BigDecimal("45"), result.getGoalScore());

        verify(studentRepository).findById(studentId);
        verify(interventionRepository).save(any(Intervention.class));
    }

    @Test
    void createIntervention_StudentNotFound_ThrowsException() {
        // Arrange
        CreateInterventionRequest request = new CreateInterventionRequest();
        request.setStudentId(studentId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            interventionService.createIntervention(request);
        });

        assertEquals("Student not found with ID: " + studentId, exception.getMessage());
        verify(studentRepository).findById(studentId);
        verifyNoInteractions(interventionRepository);
    }

    @Test
    void updateInterventionProgress_ValidUpdate_ReturnsUpdatedIntervention() {
        // Arrange
        Intervention existingIntervention = new Intervention();
        existingIntervention.setId(interventionId);
        existingIntervention.setStudent(testStudent);
        existingIntervention.setStartDate(LocalDate.now().minusMonths(1));
        existingIntervention.setTargetCompletionDate(LocalDate.now().plusMonths(2));
        existingIntervention.setStartScore(new BigDecimal("75"));
        existingIntervention.setCurrentScore(new BigDecimal("75"));
        existingIntervention.setGoalScore(new BigDecimal("45"));

        InterventionProgressUpdate update = new InterventionProgressUpdate();
        update.setCurrentScore(new BigDecimal("60"));
        update.setUpdatedOn(LocalDate.now());
        update.setStatus("ON_TRACK");

        when(interventionRepository.findById(interventionId)).thenReturn(Optional.of(existingIntervention));
        when(interventionRepository.save(any(Intervention.class))).thenReturn(existingIntervention);

        // Act
        Intervention result = interventionService.updateInterventionProgress(interventionId.toString(), update);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("60"), result.getCurrentScore());

        verify(interventionRepository, times(2)).findById(interventionId);
        verify(interventionRepository).save(existingIntervention);
    }

    @Test
    void getStudentInterventions_ValidStudentId_ReturnsInterventions() {
        // Arrange
        Intervention intervention1 = new Intervention();
        intervention1.setId(UUID.randomUUID());
        intervention1.setInterventionType("Academic Support");

        Intervention intervention2 = new Intervention();
        intervention2.setId(UUID.randomUUID());
        intervention2.setInterventionType("Behavioral Support");

        List<Intervention> interventions = Arrays.asList(intervention1, intervention2);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(interventionRepository.findByStudentId(studentId)).thenReturn(interventions);

        // Act
        List<Intervention> result = interventionService.getStudentInterventions(studentId.toString());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Academic Support", result.get(0).getInterventionType());
        assertEquals("Behavioral Support", result.get(1).getInterventionType());

        verify(interventionRepository).findByStudentId(studentId);
    }

    @Test
    void isStudentOnTrack_MidpointProgress_ReturnsTrue() {
        // Arrange
        Intervention intervention = new Intervention();
        intervention.setId(interventionId);
        intervention.setStartDate(LocalDate.now().minusDays(30)); // 30 days ago
        intervention.setTargetCompletionDate(LocalDate.now().plusDays(30)); // 30 days from now
        intervention.setStartScore(new BigDecimal("80"));
        intervention.setCurrentScore(new BigDecimal("65")); // 50% progress toward goal of 50
        intervention.setGoalScore(new BigDecimal("50"));

        when(interventionRepository.findById(interventionId)).thenReturn(Optional.of(intervention));

        // Act
        boolean result = interventionService.isStudentOnTrack(interventionId.toString());

        // Assert
        assertTrue(result); // Should be on track with 50% progress at 50% time
        verify(interventionRepository).findById(interventionId);
    }

    @Test
    void isStudentOnTrack_BehindSchedule_ReturnsFalse() {
        // Arrange
        Intervention intervention = new Intervention();
        intervention.setId(interventionId);
        intervention.setStartDate(LocalDate.now().minusDays(75)); // Started 75 days ago
        intervention.setTargetCompletionDate(LocalDate.now().plusDays(25)); // Ends in 25 days
        intervention.setStartScore(new BigDecimal("80"));
        intervention.setCurrentScore(new BigDecimal("75")); // Only 16.7% progress
        intervention.setGoalScore(new BigDecimal("50")); // Need 30 point reduction

        when(interventionRepository.findById(interventionId)).thenReturn(Optional.of(intervention));

        // Act
        boolean result = interventionService.isStudentOnTrack(interventionId.toString());

        // Assert
        assertFalse(result); // Should be behind schedule (75% time, 16.7% progress)
        verify(interventionRepository).findById(interventionId);
    }

    @Test
    void isStudentOnTrack_InterventionNotFound_ThrowsException() {
        // Arrange
        when(interventionRepository.findById(interventionId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            interventionService.isStudentOnTrack(interventionId.toString());
        });

        assertEquals("Intervention not found with ID: " + interventionId, exception.getMessage());
        verify(interventionRepository).findById(interventionId);
    }
}
