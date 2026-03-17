package com.school.School_Management_System.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"school_id", "code"})
        })
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // ✅ REQUIRED (this was missing or null before)
    @Column(nullable = false)
    private String code;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private School school;

    // getters & setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }
}
