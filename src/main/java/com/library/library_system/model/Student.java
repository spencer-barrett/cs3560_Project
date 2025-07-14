package com.library.library_system.model;

import jakarta.persistence.*;
import java.util.List;

/*
 * Model for the Student Management View
 * Handles the management of students in the library system
 * Includes CRUD operations for students
 */
@Entity
@Table(name = "student")
public class Student {
    @Id
    @Column(name = "bronco_id")
    private int broncoId;

    private String name;
    private String address;
    private String degree;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Loan> loans;

    public Student() {}

    /*
     * Getters and Setters
     */


    public String getName() {
    	return name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    public String getAddress() {
    	return address;
    }
    
    public void setAddress(String address) {
    	this.address = address;
    }
    
    public String getDegree() {
    	return degree;
    }
    
    public void setDegree(String degree) {
    	this.degree = degree;
    }

    public int getBroncoId() {
        return broncoId;
    }

    public void setBroncoId(int broncoId) {
        this.broncoId = broncoId;
    }

    /*
     * toString method to display the student's name and bronco ID
     */
    @Override
    public String toString() {
        return broncoId + " - " + name;
    }
}
