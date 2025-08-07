package com.educational.student_risk_assessment.dto;

public class JwtAuthenticationResponse {

    private String accessToken;
    private String tokenType;

    // Constructors
    public JwtAuthenticationResponse() {}

    public JwtAuthenticationResponse(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}
