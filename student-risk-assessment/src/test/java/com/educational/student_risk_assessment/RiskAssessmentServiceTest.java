package com.educational.student_risk_assessment;

import com.educational.student_risk_assessment.dto.AtRiskStudent;
import com.educational.student_risk_assessment.dto.RiskLevel;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AcademicPerformanceRepository academicPerformanceRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private BehaviorRepository behaviorRepository;

    @InjectMocks
    private RiskAssessmentService riskAssessmentService;

    private Student testStudent;
    private UUID studentId;
    private String semester;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        semester = "Fall2024";
        testStudent = new Student();
        testStudent.setId(studentId);
        testStudent.setName("John Doe");
        testStudent.setGrade("9th");
    }

    @Test
    void calculateRiskScore_HighRiskStudent_ReturnsCorrectScore() {

        AcademicPerformance academicPerformance = new AcademicPerformance();
        academicPerformance.setStudent(testStudent);
        academicPerformance.setSemester(semester);
        academicPerformance.setGrade(new BigDecimal("65"));
        academicPerformance.setStateAssessmentEla(450);


        Attendance attendance = new Attendance();
        attendance.setStudent(testStudent);
        attendance.setSemester(semester);
        attendance.setAttendanceRate(new BigDecimal("85"));
        attendance.setAbsentDays(15);
        attendance.setTardyDays(8);


        Behavior behavior = new Behavior();
        behavior.setStudent(testStudent);
        behavior.setSemester(semester);
        behavior.setDisciplinaryActions(5);
        behavior.setSuspensions(2);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(academicPerformanceRepository.findByStudentIdAndSemester(studentId, semester))
                .thenReturn(Arrays.asList(academicPerformance));
        when(attendanceRepository.findByStudentIdAndSemester(studentId, semester))
                .thenReturn(Optional.of(attendance));
        when(behaviorRepository.findByStudentIdAndSemester(studentId, semester))
                .thenReturn(Optional.of(behavior));

        StudentRiskAssessment result = riskAssessmentService.calculateRiskScore(studentId.toString(), semester);

        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals("John Doe", result.getStudentName());
        assertEquals(semester, result.getSemester());


        assertEquals(new BigDecimal("40"), result.getAcademicScore());
        assertEquals(new BigDecimal("30"), result.getAttendanceScore());
        assertEquals(new BigDecimal("20"), result.getBehaviorScore());
        assertEquals(new BigDecimal("10"), result.getTardinessScore());
        assertEquals(new BigDecimal("100"), result.getTotalRiskScore());
        assertEquals("HIGH", result.getRiskLevel());

        verify(studentRepository).findById(studentId);
        verify(academicPerformanceRepository).findByStudentIdAndSemester(studentId, semester);
        verify(attendanceRepository, times(2)).findByStudentIdAndSemester(studentId, semester);
        verify(behaviorRepository).findByStudentIdAndSemester(studentId, semester);
    }

    @Test
    void calculateRiskScore_LowRiskStudent_ReturnsCorrectScore() {

        AcademicPerformance academicPerformance = new AcademicPerformance();
        academicPerformance.setStudent(testStudent);
        academicPerformance.setSemester(semester);
        academicPerformance.setGrade(new BigDecimal("85"));
        academicPerformance.setStateAssessmentEla(550);

        Attendance attendance = new Attendance();
        attendance.setStudent(testStudent);
        attendance.setSemester(semester);
        attendance.setAttendanceRate(new BigDecimal("95"));
        attendance.setAbsentDays(3);
        attendance.setTardyDays(2);

        Behavior behavior = new Behavior();
        behavior.setStudent(testStudent);
        behavior.setSemester(semester);
        behavior.setDisciplinaryActions(1);
        behavior.setSuspensions(0);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(academicPerformanceRepository.findByStudentIdAndSemester(studentId, semester))
                .thenReturn(Arrays.asList(academicPerformance));
        when(attendanceRepository.findByStudentIdAndSemester(studentId, semester))
                .thenReturn(Optional.of(attendance));
        when(behaviorRepository.findByStudentIdAndSemester(studentId, semester))
                .thenReturn(Optional.of(behavior));


        StudentRiskAssessment result = riskAssessmentService.calculateRiskScore(studentId.toString(), semester);

        assertEquals(BigDecimal.ZERO, result.getAcademicScore());
        assertEquals(BigDecimal.ZERO, result.getAttendanceScore());
        assertEquals(BigDecimal.ZERO, result.getBehaviorScore());
        assertEquals(BigDecimal.ZERO, result.getTardinessScore());
        assertEquals(BigDecimal.ZERO, result.getTotalRiskScore());
        assertEquals("LOW", result.getRiskLevel());
        verify(studentRepository).findById(studentId);
        verify(academicPerformanceRepository).findByStudentIdAndSemester(studentId, semester);
        verify(attendanceRepository, times(2)).findByStudentIdAndSemester(studentId, semester);
        verify(behaviorRepository).findByStudentIdAndSemester(studentId, semester);
    }

    @Test
    void calculateRiskScore_StudentNotFound_ThrowsException() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            riskAssessmentService.calculateRiskScore(studentId.toString(), semester);
        });

        assertEquals("Student not found with ID: " + studentId, exception.getMessage());
        verify(studentRepository).findById(studentId);
        verifyNoInteractions(academicPerformanceRepository, attendanceRepository, behaviorRepository);
    }

    @Test
    void identifyAtRiskStudents_WithFilters_ReturnsFilteredResults() {
        Student highRiskStudent = new Student();
        highRiskStudent.setId(UUID.randomUUID());
        highRiskStudent.setName("High Risk Student");
        highRiskStudent.setGrade("10th");

        Student mediumRiskStudent = new Student();
        mediumRiskStudent.setId(UUID.randomUUID());
        mediumRiskStudent.setName("Medium Risk Student");
        mediumRiskStudent.setGrade("11th");

        when(studentRepository.findAll()).thenReturn(Arrays.asList(highRiskStudent, mediumRiskStudent));

        when(studentRepository.findById(highRiskStudent.getId())).thenReturn(Optional.of(highRiskStudent));
        when(studentRepository.findById(mediumRiskStudent.getId())).thenReturn(Optional.of(mediumRiskStudent));

        mockHighRiskData(highRiskStudent);

        mockMediumRiskData(mediumRiskStudent);


        List<AtRiskStudent> results = riskAssessmentService.identifyAtRiskStudents(semester, RiskLevel.MEDIUM);

        assertNotNull(results);
        assertEquals(2, results.size());

        AtRiskStudent highRisk = results.stream()
                .filter(s -> s.getStudentName().equals("High Risk Student"))
                .findFirst()
                .orElse(null);
        assertNotNull(highRisk);
        assertEquals("HIGH", highRisk.getRiskLevel());

        AtRiskStudent mediumRisk = results.stream()
                .filter(s -> s.getStudentName().equals("Medium Risk Student"))
                .findFirst()
                .orElse(null);
        assertNotNull(mediumRisk);
        assertEquals("MEDIUM", mediumRisk.getRiskLevel());
    }

    @Test
    void identifyAtRiskStudents_OnlyHighRisk_ReturnsFilteredResults() {
        Student highRiskStudent = new Student();
        highRiskStudent.setId(UUID.randomUUID());
        highRiskStudent.setName("High Risk Student");

        Student lowRiskStudent = new Student();
        lowRiskStudent.setId(UUID.randomUUID());
        lowRiskStudent.setName("Low Risk Student");

        when(studentRepository.findAll()).thenReturn(Arrays.asList(highRiskStudent, lowRiskStudent));

        when(studentRepository.findById(highRiskStudent.getId())).thenReturn(Optional.of(highRiskStudent));
        when(studentRepository.findById(lowRiskStudent.getId())).thenReturn(Optional.of(lowRiskStudent));

        mockHighRiskData(highRiskStudent);
        mockLowRiskData(lowRiskStudent);

        List<AtRiskStudent> results = riskAssessmentService.identifyAtRiskStudents(semester, RiskLevel.HIGH);

        assertEquals(1, results.size());
        assertEquals("High Risk Student", results.get(0).getStudentName());
        assertEquals("HIGH", results.get(0).getRiskLevel());
    }


    private void mockHighRiskData(Student student) {
        AcademicPerformance academic = new AcademicPerformance();
        academic.setGrade(new BigDecimal("65"));
        academic.setStateAssessmentEla(450);

        Attendance attendance = new Attendance();
        attendance.setAttendanceRate(new BigDecimal("85"));
        attendance.setAbsentDays(15);
        attendance.setTardyDays(8);

        Behavior behavior = new Behavior();
        behavior.setDisciplinaryActions(5);
        behavior.setSuspensions(2);

        when(academicPerformanceRepository.findByStudentIdAndSemester(student.getId(), semester))
                .thenReturn(Arrays.asList(academic));


        when(attendanceRepository.findByStudentIdAndSemester(student.getId(), semester))
                .thenReturn(Optional.of(attendance));

        when(behaviorRepository.findByStudentIdAndSemester(student.getId(), semester))
                .thenReturn(Optional.of(behavior));
    }

    private void mockMediumRiskData(Student student) {
        AcademicPerformance academic = new AcademicPerformance();
        academic.setGrade(new BigDecimal("68"));
        academic.setStateAssessmentEla(520);

        Attendance attendance = new Attendance();
        attendance.setAttendanceRate(new BigDecimal("88"));
        attendance.setAbsentDays(8);
        attendance.setTardyDays(3);

        Behavior behavior = new Behavior();
        behavior.setDisciplinaryActions(1);
        behavior.setSuspensions(0);

        when(academicPerformanceRepository.findByStudentIdAndSemester(student.getId(), semester))
                .thenReturn(Arrays.asList(academic));
        when(attendanceRepository.findByStudentIdAndSemester(student.getId(), semester))
                .thenReturn(Optional.of(attendance));
        when(behaviorRepository.findByStudentIdAndSemester(student.getId(), semester))
                .thenReturn(Optional.of(behavior));
    }

    private void mockLowRiskData(Student student) {
        AcademicPerformance academic = new AcademicPerformance();
        academic.setGrade(new BigDecimal("85"));
        academic.setStateAssessmentEla(550);

        Attendance attendance = new Attendance();
        attendance.setAttendanceRate(new BigDecimal("95"));
        attendance.setAbsentDays(3);
        attendance.setTardyDays(2);

        Behavior behavior = new Behavior();
        behavior.setDisciplinaryActions(1);
        behavior.setSuspensions(0);

        when(academicPerformanceRepository.findByStudentIdAndSemester(student.getId(), semester))
                .thenReturn(Arrays.asList(academic));

        when(attendanceRepository.findByStudentIdAndSemester(student.getId(), semester))
                .thenReturn(Optional.of(attendance));

        when(behaviorRepository.findByStudentIdAndSemester(student.getId(), semester))
                .thenReturn(Optional.of(behavior));
    }
}
