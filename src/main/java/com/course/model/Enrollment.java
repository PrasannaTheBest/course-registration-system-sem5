package com.course.model;

import java.sql.Timestamp;

/** A student's enrolment in a course, as shown on the admin "students in this course" page. */
public class Enrollment {
    private final String studentName;
    private final String studentEmail;
    private final Timestamp enrolledAt;

    public Enrollment(String studentName, String studentEmail, Timestamp enrolledAt) {
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.enrolledAt = enrolledAt;
    }

    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public Timestamp getEnrolledAt() { return enrolledAt; }
}
