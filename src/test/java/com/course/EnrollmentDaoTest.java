package com.course;

import com.course.dao.CourseDAO;
import com.course.dao.EnrollmentDAO;
import com.course.dao.EnrollmentDAO.Result;
import com.course.dao.UserDAO;
import com.course.listener.AppInitListener;
import com.course.model.Course;
import com.course.model.User;
import com.course.util.PasswordUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Runs the real DAO code against an in-memory H2 database in MySQL mode. */
class EnrollmentDaoTest {
    private static final UserDAO users = new UserDAO();
    private static final CourseDAO courses = new CourseDAO();
    private static final EnrollmentDAO enrollments = new EnrollmentDAO();

    @BeforeAll
    static void setUp() throws Exception {
        System.setProperty("db.driver", "org.h2.Driver");
        System.setProperty("db.url", "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1");
        System.setProperty("db.user", "sa");
        System.setProperty("db.password", "");
        AppInitListener.createTables();
        AppInitListener.seed();
    }

    private int newStudent(String email) throws Exception {
        assertTrue(users.create("Test " + email, email, PasswordUtil.hash("secret1"), User.STUDENT));
        return users.findByEmail(email).getId();
    }

    private int newCourse(String code, int seats) throws Exception {
        return newCourse(code, seats, 3);
    }

    private int newCourse(String code, int seats, int credits) throws Exception {
        Course c = new Course();
        c.setCode(code);
        c.setTitle("Title " + code);
        c.setInstructor("Prof X");
        c.setCredits(credits);
        c.setSeats(seats);
        assertTrue(courses.save(c));
        return courses.search(code).get(0).getId();
    }

    @Test
    void seedCreatesAdminAndCourses() throws Exception {
        User admin = users.findByEmail(AppInitListener.ADMIN_EMAIL);
        assertNotNull(admin);
        assertTrue(admin.isAdmin());
        assertTrue(PasswordUtil.verify(AppInitListener.ADMIN_PASSWORD, admin.getPasswordHash()));
        assertFalse(PasswordUtil.verify("wrong", admin.getPasswordHash()));
        assertTrue(courses.count() >= 6);
    }

    @Test
    void duplicateEmailAndDuplicateCourseCodeAreRejected() throws Exception {
        newStudent("dup@example.com");
        assertFalse(users.create("Again", "DUP@example.com", "x", User.STUDENT));

        newCourse("DUP100", 5);
        Course c = new Course();
        c.setCode("DUP100");
        c.setTitle("Other");
        c.setInstructor("Y");
        c.setCredits(2);
        c.setSeats(5);
        assertFalse(courses.save(c));
    }

    @Test
    void enrollDropAndDoubleEnroll() throws Exception {
        int s = newStudent("a@example.com");
        int c = newCourse("ENR101", 3);

        assertEquals(Result.ENROLLED, enrollments.enroll(s, c));
        assertEquals(Result.ALREADY_ENROLLED, enrollments.enroll(s, c));
        assertTrue(enrollments.enrolledCourseIds(s).contains(c));
        assertEquals(1, courses.findById(c).getEnrolled());
        assertEquals(1, enrollments.coursesForUser(s).size());
        assertEquals(1, enrollments.studentsForCourse(c).size());

        assertTrue(enrollments.drop(s, c));
        assertFalse(enrollments.drop(s, c));
        assertEquals(0, courses.findById(c).getEnrolled());
    }

    @Test
    void fullCourseRejectsExtraStudents() throws Exception {
        int c = newCourse("FULL101", 2);
        int s1 = newStudent("f1@example.com");
        int s2 = newStudent("f2@example.com");
        int s3 = newStudent("f3@example.com");

        assertEquals(Result.ENROLLED, enrollments.enroll(s1, c));
        assertEquals(Result.ENROLLED, enrollments.enroll(s2, c));
        assertEquals(Result.FULL, enrollments.enroll(s3, c));
        assertTrue(courses.findById(c).isFull());
        assertEquals(0, courses.findById(c).getSeatsLeft());

        // dropping frees a seat again
        assertTrue(enrollments.drop(s1, c));
        assertEquals(Result.ENROLLED, enrollments.enroll(s3, c));
    }

    @Test
    void creditLimitIsEnforced() throws Exception {
        int s = newStudent("credits@example.com");
        // 4 courses x 6 credits = exactly 24 credits, which is allowed
        for (int i = 1; i <= 4; i++) {
            assertEquals(Result.ENROLLED, enrollments.enroll(s, newCourse("CRD10" + i, 10, 6)));
        }
        int extra = newCourse("CRD105", 10, 1);
        assertEquals(Result.CREDIT_LIMIT, enrollments.enroll(s, extra));

        // dropping a course makes room again
        int first = enrollments.coursesForUser(s).get(0).getId();
        assertTrue(enrollments.drop(s, first));
        assertEquals(Result.ENROLLED, enrollments.enroll(s, extra));
    }

    @Test
    void deletingACourseRemovesItsEnrollments() throws Exception {
        int s = newStudent("del@example.com");
        int c = newCourse("DEL101", 5);
        assertEquals(Result.ENROLLED, enrollments.enroll(s, c));
        courses.delete(c);
        assertNull(courses.findById(c));
        assertTrue(enrollments.enrolledCourseIds(s).isEmpty());
        assertEquals(Result.COURSE_NOT_FOUND, enrollments.enroll(s, c));
    }

    @Test
    void searchMatchesCodeTitleAndInstructor() throws Exception {
        assertFalse(courses.search("data structures").isEmpty());
        assertFalse(courses.search("cs201").isEmpty());
        assertFalse(courses.search("neha").isEmpty());
        assertTrue(courses.search("zzzz-no-match").isEmpty());
    }
}
