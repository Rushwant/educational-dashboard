package com.educational.student_risk_assessment.controller;

import com.educational.student_risk_assessment.dto.JwtAuthenticationResponse;
import com.educational.student_risk_assessment.dto.LoginRequest;
import com.educational.student_risk_assessment.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and JWT token management")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Operation(
            summary = "User login",
            description = "Authenticate user credentials and return JWT token for API access"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful - JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, "Bearer"));
    }
}
