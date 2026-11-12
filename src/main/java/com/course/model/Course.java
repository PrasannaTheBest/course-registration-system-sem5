package com.course.model;

import java.sql.Timestamp;

public class Course {
    private int id;
    private String code;
    private String title;
    private String instructor;
    private int credits;
    private int seats;
    private String description;
    /** Number of students currently enrolled (filled in by queries). */
    private int enrolled;
    /** When the current student enrolled (only set by "my courses" queries). */
    private Timestamp enrolledAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getEnrolled() { return enrolled; }
    public void setEnrolled(int enrolled) { this.enrolled = enrolled; }
    public Timestamp getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Timestamp enrolledAt) { this.enrolledAt = enrolledAt; }

    public int getSeatsLeft() { return Math.max(0, seats - enrolled); }
    public boolean isFull() { return enrolled >= seats; }
}
