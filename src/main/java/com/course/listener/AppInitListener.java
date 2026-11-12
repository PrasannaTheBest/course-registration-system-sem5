package com.course.listener;

import com.course.dao.CourseDAO;
import com.course.dao.UserDAO;
import com.course.model.Course;
import com.course.model.User;
import com.course.util.DBUtil;
import com.course.util.PasswordUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Runs once when the web app starts: creates the tables and seeds first-run data. */
@WebListener
public class AppInitListener implements ServletContextListener {

    public static final String ADMIN_EMAIL = "admin@college.edu";
    public static final String ADMIN_PASSWORD = "admin123";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            createTables();
            seed();
            event.getServletContext().log("Course registration: database ready.");
        } catch (SQLException | IOException e) {
            throw new IllegalStateException(
                    "Could not initialise the database. Check db.properties (url, user, password) "
                            + "and that MySQL is running: " + e.getMessage(), e);
        }
    }

    /** Runs the same start-up steps; also used by the tests. */
    public static void createTables() throws SQLException, IOException {
        try (InputStream in = AppInitListener.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IOException("schema.sql not found on the classpath");
            }
            StringBuilder cleaned = new StringBuilder();
            for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                if (!line.trim().startsWith("--")) {
                    cleaned.append(line).append('\n');
                }
            }
            try (Connection c = DBUtil.getConnection(); Statement st = c.createStatement()) {
                for (String sql : cleaned.toString().split(";")) {
                    if (!sql.trim().isEmpty()) {
                        st.execute(sql.trim());
                    }
                }
            }
        }
    }

    public static void seed() throws SQLException {
        UserDAO users = new UserDAO();
        if (users.count() == 0) {
            users.create("Administrator", ADMIN_EMAIL, PasswordUtil.hash(ADMIN_PASSWORD), User.ADMIN);
        }
        CourseDAO courses = new CourseDAO();
        if (courses.count() == 0) {
            add(courses, "CS101", "Introduction to Programming", "Dr. Anita Sharma", 4, 40,
                    "Basics of programming using Java: variables, control flow, methods and arrays.");
            add(courses, "CS201", "Data Structures", "Prof. Rahul Mehta", 4, 35,
                    "Lists, stacks, queues, trees, graphs and their algorithms.");
            add(courses, "CS301", "Database Management Systems", "Dr. Neha Verma", 3, 30,
                    "Relational model, SQL, normalisation and transactions.");
            add(courses, "CS310", "Web Technologies", "Prof. Arjun Rao", 3, 30,
                    "HTML, CSS, JavaScript, Servlets and JSP.");
            add(courses, "CS320", "Operating Systems", "Dr. Kavita Joshi", 4, 25,
                    "Processes, threads, scheduling, memory management and file systems.");
            add(courses, "MA210", "Discrete Mathematics", "Prof. Sanjay Gupta", 3, 45,
                    "Logic, sets, relations, combinatorics and graph theory.");
        }
    }

    private static void add(CourseDAO dao, String code, String title, String instructor,
                            int credits, int seats, String description) throws SQLException {
        Course c = new Course();
        c.setCode(code);
        c.setTitle(title);
        c.setInstructor(instructor);
        c.setCredits(credits);
        c.setSeats(seats);
        c.setDescription(description);
        dao.save(c);
    }
}
