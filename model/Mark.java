package com.school.School_Management_System.model;

import jakarta.persistence.*;

@Entity
@Table(name="marks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"assessment_id","student_user_id"}))
public class Mark {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="assessment_id")
    private Assessment assessment;

    @ManyToOne(optional=false) @JoinColumn(name="student_user_id")
    private User student;

    @Column(nullable=false)
    private int marks;

    @Column(length=500)
    private String feedback;

    @ManyToOne(optional=false) @JoinColumn(name="entered_by_teacher_id")
    private User enteredBy;

    public Long getId() { return id; }
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public User getEnteredBy() { return enteredBy; }
    public void setEnteredBy(User enteredBy) { this.enteredBy = enteredBy; }
}
