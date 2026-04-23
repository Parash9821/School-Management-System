package com.school.School_Management_System.model;

import jakarta.persistence.*;

@Entity
@Table(name="teaching_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"school_id","classroom_id","subject_id"}))
public class TeachingAssignment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="school_id")
    private School school;

    @ManyToOne(optional=false) @JoinColumn(name="classroom_id")
    private Classroom classroom;

    @ManyToOne(optional=false) @JoinColumn(name="subject_id")
    private Subject subject;

    @ManyToOne(optional=false) @JoinColumn(name="teacher_user_id")
    private User teacher;

    public Long getId() { return id; }
    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }
    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }
}
