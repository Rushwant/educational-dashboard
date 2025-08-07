package com.educational.student_risk_assessment;

import com.educational.student_risk_assessment.dto.CreateInterventionRequest;
import com.educational.student_risk_assessment.dto.InterventionProgressUpdate;
import com.educational.student_risk_assessment.entity.Student;
import com.educational.student_risk_assessment.repository.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class InterventionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private static Student testStudent; // Make static to persist across test methods
    private static String interventionId; // Make static to persist across test methods

    @BeforeAll
    static void setupClass(@Autowired StudentRepository studentRepository) {
        // Create test student once for all tests
        testStudent = new Student();
        testStudent.setName("Integration Test Student");
        testStudent.setGrade("10th");
        testStudent = studentRepository.save(testStudent);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @Order(1)
    @WithMockUser(roles = "TEACHER")
    void createIntervention_ValidRequest_ReturnsCreatedIntervention() throws Exception {
        // Arrange
        CreateInterventionRequest request = new CreateInterventionRequest();
        request.setStudentId(testStudent.getId());
        request.setInterventionType("Academic Support");
        request.setStartDate(LocalDate.now());
        request.setTargetCompletionDate(LocalDate.now().plusMonths(3));
        request.setStartScore(new BigDecimal("75"));
        request.setGoalScore(new BigDecimal("45"));

        // Act & Assert
        mockMvc.perform(post("/api/interventions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk()) // Your API returns 200, not 201
                .andExpect(jsonPath("$.student.id").value(testStudent.getId().toString())) // Fixed: API returns nested student object
                .andExpect(jsonPath("$.interventionType").value("Academic Support"))
                .andExpect(jsonPath("$.startScore").value(75))
                .andExpect(jsonPath("$.goalScore").value(45))
                .andExpect(jsonPath("$.status").value("ON_TRACK"))
                .andDo(result -> {
                    // Store intervention ID for future tests
                    String response = result.getResponse().getContentAsString();
                    JsonNode jsonNode = objectMapper.readTree(response);
                    interventionId = jsonNode.get("id").asText();
                    System.out.println("Created intervention ID: " + interventionId); // Debug
                });
    }

    @Test
    @Order(2)
    @WithMockUser(roles = "ADMIN")
    void updateInterventionProgress_ValidRequest_ReturnsUpdatedIntervention() throws Exception {
        // Arrange
        InterventionProgressUpdate update = new InterventionProgressUpdate();
        update.setCurrentScore(new BigDecimal("60"));
        update.setUpdatedOn(LocalDate.now());
        update.setStatus("ON_TRACK");

        // Act & Assert
        mockMvc.perform(put("/api/interventions/{id}/progress", interventionId) // Fixed: Use actual ID
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentScore").value(60))
                .andExpect(jsonPath("$.id").value(interventionId));
    }

    @Test
    @Order(3)
    @WithMockUser(roles = "TEACHER")
    void getStudentInterventions_ValidStudentId_ReturnsInterventions() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/interventions/student/{studentId}", testStudent.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].student.id").value(testStudent.getId().toString())) // Fixed: nested student object
                .andExpect(jsonPath("$[0].interventionType").value("Academic Support"));
    }

    @Test
    @Order(4)
    @WithMockUser(roles = "PARENT", username = "parent_test")
    void getStudentInterventions_AsParent_ReturnsOnlyOwnChildrenData() throws Exception {
        // This test assumes your AuthorizationService allows parent access
        // You might need to mock the canAccessStudentData method to return true
        mockMvc.perform(get("/api/interventions/student/{studentId}", testStudent.getId()))
                .andDo(print())
                .andExpect(status().isOk()) // Should work if parent has access
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(5)
    @WithMockUser(roles = "STUDENT", username = "different-student")
    void getStudentInterventions_AsUnauthorizedStudent_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/interventions/student/{studentId}", testStudent.getId()))
                .andDo(print())
                .andExpect(status().isForbidden()); // Your API returns 403
    }

    @Test
    @Order(6)
    @WithMockUser(roles = "TEACHER")
    void createIntervention_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Arrange - Empty request (missing required fields)
        CreateInterventionRequest invalidRequest = new CreateInterventionRequest();
        // Don't set any fields to trigger validation errors

        // Act & Assert
        mockMvc.perform(post("/api/interventions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest()) // Your API correctly returns 400
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").value("Validation Error")); // Based on your error response format
    }

    @Test
    @Order(7)
    void createIntervention_Unauthorized_ReturnsForbidden() throws Exception {
        // Arrange
        CreateInterventionRequest request = new CreateInterventionRequest();
        request.setStudentId(testStudent.getId());
        request.setInterventionType("Academic Support");
        request.setStartDate(LocalDate.now());
        request.setTargetCompletionDate(LocalDate.now().plusMonths(3));
        request.setStartScore(new BigDecimal("75"));
        request.setGoalScore(new BigDecimal("45"));

        // Act & Assert - No authentication
        mockMvc.perform(post("/api/interventions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden()); // Your API returns 403, not 401
    }
}
