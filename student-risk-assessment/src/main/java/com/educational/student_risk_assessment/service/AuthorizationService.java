package com.educational.student_risk_assessment.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public boolean canAccessStudentData(String studentId, Authentication authentication) {
        // Placeholder implementation for parent-child relationship validation
        // In a real system, this would check if the authenticated parent
        // has access to this specific student's data

        String username = authentication.getName();

        // For demo purposes, assume parent usernames follow pattern: "parent_[studentId]"
        // or implement actual parent-child relationship lookup
        return username.startsWith("parent_") ||
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
