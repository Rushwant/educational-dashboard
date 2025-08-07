package com.educational.student_risk_assessment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "academic_performance",
        indexes = {
                @Index(name = "idx_academic_student_semester", columnList = "student_id, semester"),
                @Index(name = "idx_academic_student_id", columnList = "student_id"),
                @Index(name = "idx_academic_semester", columnList = "semester"),
                @Index(name = "idx_academic_grade", columnList = "grade"),
                @Index(name = "idx_academic_ela_score", columnList = "state_assessment_ela"),
                @Index(name = "idx_academic_math_score", columnList = "state_assessment_math")
        })
public class AcademicPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    @Size(max = 20)
    @Column(name = "semester", length = 20)
    private String semester;

    @Size(max = 50)
    @Column(name = "course", length = 50)
    private String course;

    @Column(name = "grade", precision = 5, scale = 2)
    private BigDecimal grade;

    @Column(name = "state_assessment_ela")
    private Integer stateAssessmentEla;

    @Column(name = "state_assessment_math")
    private Integer stateAssessmentMath;

    // Constructors
    public AcademicPerformance() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public BigDecimal getGrade() { return grade; }
    public void setGrade(BigDecimal grade) { this.grade = grade; }

    public Integer getStateAssessmentEla() { return stateAssessmentEla; }
    public void setStateAssessmentEla(Integer stateAssessmentEla) { this.stateAssessmentEla = stateAssessmentEla; }

    public Integer getStateAssessmentMath() { return stateAssessmentMath; }
    public void setStateAssessmentMath(Integer stateAssessmentMath) { this.stateAssessmentMath = stateAssessmentMath; }
}
