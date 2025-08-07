package com.educational.student_risk_assessment;

import com.educational.student_risk_assessment.entity.*;
import com.educational.student_risk_assessment.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class RiskAssessmentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AcademicPerformanceRepository academicPerformanceRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private BehaviorRepository behaviorRepository;

    private MockMvc mockMvc;
    private Student highRiskStudent;
    private String semester = "Fall2024";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        setupTestData();
    }

    void setupTestData() {
        // Create high-risk student with complete data
        highRiskStudent = new Student();
        highRiskStudent.setName("High Risk Test Student");
        highRiskStudent.setGrade("9th");
        highRiskStudent = studentRepository.save(highRiskStudent);

        // Create high-risk academic performance
        AcademicPerformance academic = new AcademicPerformance();
        academic.setStudent(highRiskStudent);
        academic.setSemester(semester);
        academic.setGrade(new BigDecimal("65")); // Below 70%
        academic.setCourse("Math");
        academic.setStateAssessmentEla(450); // Below 500
        academic.setStateAssessmentMath(480); // Below 500
        academicPerformanceRepository.save(academic);

        // Create high-risk attendance
        Attendance attendance = new Attendance();
        attendance.setStudent(highRiskStudent);
        attendance.setSemester(semester);
        attendance.setAttendanceRate(new BigDecimal("85")); // Below 90%
        attendance.setAbsentDays(15); // Over 10
        attendance.setTardyDays(8); // Over 5
        attendanceRepository.save(attendance);

        // Create high-risk behavior
        Behavior behavior = new Behavior();
        behavior.setStudent(highRiskStudent);
        behavior.setSemester(semester);
        behavior.setDisciplinaryActions(5); // Over 2
        behavior.setSuspensions(2); // Over 0
        behaviorRepository.save(behavior);
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void getStudentRiskAssessment_ValidStudent_ReturnsAssessment() throws Exception {
        mockMvc.perform(get("/api/risk-assessment/students/{studentId}", highRiskStudent.getId())
                        .param("semester", semester))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(highRiskStudent.getId().toString()))
                .andExpect(jsonPath("$.studentName").value("High Risk Test Student"))
                .andExpect(jsonPath("$.semester").value(semester))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.totalRiskScore").value(100))
                .andExpect(jsonPath("$.academicScore").value(40))
                .andExpect(jsonPath("$.attendanceScore").value(30))
                .andExpect(jsonPath("$.behaviorScore").value(20))
                .andExpect(jsonPath("$.tardinessScore").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAtRiskStudents_WithFilters_ReturnsFilteredResults() throws Exception {
        mockMvc.perform(get("/api/risk-assessment/at-risk")
                        .param("semester", semester)
                        .param("minimumRisk", "MEDIUM")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].studentName").value("High Risk Test Student"))
                .andExpect(jsonPath("$.content[0].riskLevel").value("HIGH"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getStudentRiskAssessment_AsStudent_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/risk-assessment/students/{studentId}", highRiskStudent.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void getAtRiskStudents_Unauthorized_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/risk-assessment/at-risk"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
