package com.educational.student_risk_assessment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    @Size(max = 20)
    @Column(name = "semester", length = 20)
    private String semester;

    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate;

    @Column(name = "absent_days")
    private Integer absentDays;

    @Column(name = "tardy_days")
    private Integer tardyDays;

    // Constructors
    public Attendance() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public BigDecimal getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(BigDecimal attendanceRate) { this.attendanceRate = attendanceRate; }

    public Integer getAbsentDays() { return absentDays; }
    public void setAbsentDays(Integer absentDays) { this.absentDays = absentDays; }

    public Integer getTardyDays() { return tardyDays; }
    public void setTardyDays(Integer tardyDays) { this.tardyDays = tardyDays; }
}
