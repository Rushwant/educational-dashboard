package com.educational.student_risk_assessment.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public boolean canAccessStudentData(String studentId, Authentication authentication) {


        String username = authentication.getName();

        return username.startsWith("parent_") ||
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
