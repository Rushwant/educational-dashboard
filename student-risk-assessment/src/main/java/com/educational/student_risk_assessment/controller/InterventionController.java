package com.educational.student_risk_assessment.controller;

import com.educational.student_risk_assessment.dto.CreateInterventionRequest;
import com.educational.student_risk_assessment.dto.InterventionProgressUpdate;
import com.educational.student_risk_assessment.entity.Intervention;
import com.educational.student_risk_assessment.service.InterventionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interventions")
@Tag(name = "Interventions", description = "Intervention management and progress tracking")
@SecurityRequirement(name = "Bearer Authentication")
public class InterventionController {

    @Autowired
    private InterventionService interventionService;

    @Operation(
            summary = "Create new intervention",
            description = "Create a new intervention plan for an at-risk student"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Intervention created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Teachers and Admins only")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Intervention> createIntervention(
            @Valid @RequestBody CreateInterventionRequest request) {

        Intervention intervention = interventionService.createIntervention(request);
        return ResponseEntity.ok(intervention);
    }

    @Operation(
            summary = "Update intervention progress",
            description = "Update the current progress score for an existing intervention"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid progress data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Teachers and Admins only"),
            @ApiResponse(responseCode = "404", description = "Intervention not found")
    })
    @PutMapping("/{id}/progress")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Intervention> updateProgress(
            @Parameter(description = "Intervention UUID", required = true)
            @PathVariable String id,
            @Valid @RequestBody InterventionProgressUpdate update) {

        Intervention intervention = interventionService.updateInterventionProgress(id, update);
        return ResponseEntity.ok(intervention);
    }

    @Operation(
            summary = "Get student interventions",
            description = "Retrieve all interventions for a specific student (with role-based access control)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interventions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions for this student"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN') or " +
            "(hasRole('PARENT') and @authorizationService.canAccessStudentData(#studentId, authentication)) or " +
            "(hasRole('STUDENT') and #studentId == authentication.name)")
    public ResponseEntity<List<Intervention>> getStudentInterventions(
            @Parameter(description = "Student UUID", required = true)
            @PathVariable String studentId,
            Authentication authentication) {

        List<Intervention> interventions = interventionService.getStudentInterventions(studentId);
        return ResponseEntity.ok(interventions);
    }
}
