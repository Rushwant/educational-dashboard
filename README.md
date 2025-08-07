# Educational Dashboard - Student Risk Assessment & Intervention System

## Overview

The Educational Dashboard is a comprehensive Spring Boot application designed to identify at-risk students and manage intervention programs to improve student outcomes. The system calculates risk scores based on academic performance, attendance, behavior, and tardiness data, then provides automated intervention recommendations and progress tracking.

## 🚀 Setup Instructions

### Prerequisites

- **Java 17** or higher
- **PostgreSQL 12+** database server
- **Maven 3.8+**
- **Git**

### Step 1: Clone the Repository

    git clone https://github.com/Rushwant/educational-dashboard.git
    cd educational-dashboard


### Step 2: Database Setup

1. **Create PostgreSQL Database:**
   
        CREATE DATABASE educational_dashboard;
        CREATE USER dashboard_user WITH ENCRYPTED PASSWORD 'your_password';
        GRANT ALL PRIVILEGES ON DATABASE educational_dashboard TO dashboard_user;

2. **Set Environment Variables (Optional):**
   
        export DB_USERNAME=dashboard_user
        export DB_PASSWORD=your_password
        export DB_URL=jdbc:postgresql://localhost:5432/educational_dashboard
        export JWT_SECRET=your_jwt_secret_key_minimum_256_bits

*Note: If you don't set environment variables, the application will use default values from `application.yml`*

### Step 3: Build and Run

1. **Install Dependencies:**
   
        mvn clean install
   
2. **Run Tests:**
    
        mvn test

3. **Start Application:**

        mvn spring-boot:run


The application will start on `http://localhost:8080`

### Step 4: API Documentation

Once running, access the interactive API documentation at:
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

### Step 5: Authentication

1. **Login to get JWT token:**
        POST /api/auth/login
        Content-Type: application/json

        {
        "username": "admin",
        "password": "password"
        }
        
2. **Use token in subsequent requests:**
 
        Authorization: Bearer <your-jwt-token>

### Sample Data for Testing

The application includes a `TestController` with endpoints to create sample data:

        GET /api/test/create-high-risk-student
        GET /api/test/create-sample-data
        GET /api/test/bulk-risk-assessment


### Database Tables

The application automatically creates the following tables on startup:
- `students` - Student information
- `academic_performance` - Course grades and state assessment scores
- `attendance` - Attendance rates and absence tracking
- `behavior` - Disciplinary actions and suspensions
- `interventions` - Intervention programs and progress tracking

### Default Configuration

The application uses these default settings (can be overridden with environment variables):
- **Database:** `localhost:5432/postgres` with user `postgres`
- **JWT Secret:** Default development key (change for production)
- **JWT Expiration:** 24 hours
- **Port:** 8080


## 📊 Key Features

- **Risk Assessment:** Weighted scoring algorithm (40% academic, 30% attendance, 20% behavior, 10% tardiness)
- **Role-Based Access:** Different permissions for Teachers, Admins, Parents, and Students
- **Intervention Tracking:** Create and monitor intervention programs with progress tracking
- **Automated Recommendations:** System suggests interventions based on risk factors
- **RESTful API:** Complete REST API with OpenAPI documentation
- **Comprehensive Testing:** Unit and integration tests with high coverage

## 🔧 Technology Stack

- **Backend:** Spring Boot 3.2+, Java 17
- **Database:** PostgreSQL with JPA/Hibernate
- **Security:** Spring Security with JWT authentication
- **Documentation:** OpenAPI 3 / Swagger UI
- **Testing:** JUnit 5, Mockito, TestContainers
- **Build Tool:** Maven

This setup provides a complete educational dashboard system ready for development and testing environments.
