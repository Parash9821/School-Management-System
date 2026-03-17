package com.school.School_Management_System.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="assessments")
public class Assessment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="school_id")
    private School school;

    @ManyToOne(optional=false) @JoinColumn(name="classroom_id")
    private Classroom classroom;

    @ManyToOne(optional=false) @JoinColumn(name="subject_id")
    private Subject subject;

    @Column(nullable=false, length=30)
    private String type; // "TEST", "MID", "FINAL"

    @Column(nullable=false)
    private int maxMarks;

    private LocalDate heldOn;

    @ManyToOne(optional=false) @JoinColumn(name="created_by_teacher_id")
    private User createdBy;

    public Long getId() { return id; }
    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }
    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }
    public LocalDate getHeldOn() { return heldOn; }
    public void setHeldOn(LocalDate heldOn) { this.heldOn = heldOn; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}
