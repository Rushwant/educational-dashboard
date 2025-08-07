package com.educational.student_risk_assessment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "behavior")
public class Behavior {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    @Size(max = 20)
    @Column(name = "semester", length = 20)
    private String semester;

    @Column(name = "disciplinary_actions")
    private Integer disciplinaryActions = 0;

    @Column(name = "suspensions")
    private Integer suspensions = 0;

    // Constructors
    public Behavior() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public Integer getDisciplinaryActions() { return disciplinaryActions; }
    public void setDisciplinaryActions(Integer disciplinaryActions) { this.disciplinaryActions = disciplinaryActions; }

    public Integer getSuspensions() { return suspensions; }
    public void setSuspensions(Integer suspensions) { this.suspensions = suspensions; }
}
