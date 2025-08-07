package com.educational.student_risk_assessment.controller;

import com.educational.student_risk_assessment.dto.InterventionRecommendation;
import com.educational.student_risk_assessment.dto.AtRiskStudent;
import com.educational.student_risk_assessment.dto.RiskLevel;
import com.educational.student_risk_assessment.dto.StudentRiskAssessment;
import com.educational.student_risk_assessment.service.RiskAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk-assessment")
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
@Tag(name = "Risk Assessment", description = "Student risk assessment and at-risk student identification")
@SecurityRequirement(name = "Bearer Authentication")
public class RiskAssessmentController {

    @Autowired
    private RiskAssessmentService riskAssessmentService;

    @Operation(
            summary = "Get student risk assessment",
            description = "Calculate and return comprehensive risk assessment for a specific student"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Risk assessment calculated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @GetMapping("/students/{studentId}")
    public ResponseEntity<StudentRiskAssessment> getStudentRiskAssessment(
            @Parameter(description = "Student UUID", required = true)
            @PathVariable String studentId,
            @Parameter(description = "Semester (e.g., '2024-Fall'). If not provided, uses current semester")
            @RequestParam(required = false) String semester) {

        String semesterToUse = semester != null ? semester : "2024-Fall";

        StudentRiskAssessment assessment = riskAssessmentService.calculateRiskScore(
                studentId, semesterToUse
        );

        return ResponseEntity.ok(assessment);
    }

    @Operation(
            summary = "Get at-risk students",
            description = "Retrieve paginated list of students who meet the specified risk criteria"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "At-risk students retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/at-risk")
    public ResponseEntity<Page<AtRiskStudent>> getAtRiskStudents(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Semester filter")
            @RequestParam(required = false) String semester,
            @Parameter(description = "Minimum risk level filter")
            @RequestParam(required = false) RiskLevel minimumRisk) {

        String semesterToUse = semester != null ? semester : "2024-Fall";

        List<AtRiskStudent> atRiskStudents = riskAssessmentService.identifyAtRiskStudents(
                semesterToUse, minimumRisk
        );


        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), atRiskStudents.size());

        List<AtRiskStudent> pagedContent = atRiskStudents.subList(start, end);
        Page<AtRiskStudent> pagedResult = new PageImpl<>(pagedContent, pageable, atRiskStudents.size());

        return ResponseEntity.ok(pagedResult);
    }


    @Operation(
            summary = "Get automated intervention recommendations for student",
            description = "Suggest interventions for a student based on risk profile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interventions recommended"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{studentId}/recommendations")
    public ResponseEntity<List<InterventionRecommendation>> getRecommendations(
            @PathVariable String studentId,
            @RequestParam(required = false) String semester) {

        String semesterToUse = (semester != null) ? semester : "2024-Fall";
        List<InterventionRecommendation> recommendations =
                riskAssessmentService.recommendInterventionsForStudent(studentId, semesterToUse);

        return ResponseEntity.ok(recommendations);
    }
}
