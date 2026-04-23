package com.school.School_Management_System.model;

import jakarta.persistence.*;

@Entity
@Table(name = "schools")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String slug; // e.g. "everest-school"

    @Column(nullable = false, length = 150)
    private String name;

    @Lob
    private String brandingJson; // theme config for React

    @Lob
    private String aboutHtml;

    public School() {}

    // getters/setters
    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrandingJson() { return brandingJson; }
    public void setBrandingJson(String brandingJson) { this.brandingJson = brandingJson; }
    public String getAboutHtml() { return aboutHtml; }
    public void setAboutHtml(String aboutHtml) { this.aboutHtml = aboutHtml; }
}
