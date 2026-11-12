package com.course.dao;

import com.course.model.Course;
import com.course.model.Enrollment;
import com.course.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnrollmentDAO {

    public enum Result { ENROLLED, FULL, ALREADY_ENROLLED, COURSE_NOT_FOUND }

    /**
     * Enrols a student in a course inside one transaction. The course row is locked
     * (SELECT ... FOR UPDATE) so two students can never take the last seat at once.
     */
    public Result enroll(int userId, int courseId) throws SQLException {
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                int seats;
                try (PreparedStatement ps = c.prepareStatement("SELECT seats FROM courses WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, courseId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return Result.COURSE_NOT_FOUND;
                        }
                        seats = rs.getInt(1);
                    }
                }
                if (count(c, "SELECT COUNT(*) FROM enrollments WHERE user_id = ? AND course_id = ?", userId, courseId) > 0) {
                    c.rollback();
                    return Result.ALREADY_ENROLLED;
                }
                if (count(c, "SELECT COUNT(*) FROM enrollments WHERE course_id = ?", courseId) >= seats) {
                    c.rollback();
                    return Result.FULL;
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO enrollments (user_id, course_id) VALUES (?, ?)")) {
                    ps.setInt(1, userId);
                    ps.setInt(2, courseId);
                    ps.executeUpdate();
                }
                c.commit();
                return Result.ENROLLED;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** @return true if an enrolment was removed. */
    public boolean drop(int userId, int courseId) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM enrollments WHERE user_id = ? AND course_id = ?")) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() > 0;
        }
    }

    public Set<Integer> enrolledCourseIds(int userId) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT course_id FROM enrollments WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    /** Courses a student is enrolled in, newest first. */
    public List<Course> coursesForUser(int userId) throws SQLException {
        String sql = "SELECT c.id, c.code, c.title, c.instructor, c.credits, c.seats, c.description, "
                + "(SELECT COUNT(*) FROM enrollments x WHERE x.course_id = c.id) AS enrolled, e.enrolled_at "
                + "FROM enrollments e JOIN courses c ON c.id = e.course_id "
                + "WHERE e.user_id = ? ORDER BY e.enrolled_at DESC, c.code";
        List<Course> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Course course = CourseDAO.map(rs);
                    course.setEnrolledAt(rs.getTimestamp("enrolled_at"));
                    list.add(course);
                }
            }
        }
        return list;
    }

    /** Students enrolled in a course (admin view). */
    public List<Enrollment> studentsForCourse(int courseId) throws SQLException {
        String sql = "SELECT u.name, u.email, e.enrolled_at FROM enrollments e "
                + "JOIN users u ON u.id = e.user_id WHERE e.course_id = ? ORDER BY e.enrolled_at, u.name";
        List<Enrollment> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Enrollment(rs.getString(1), rs.getString(2), rs.getTimestamp(3)));
                }
            }
        }
        return list;
    }

    /** Headline numbers for the admin dashboard. */
    public Map<String, Integer> stats() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        try (Connection c = DBUtil.getConnection()) {
            stats.put("courses", count(c, "SELECT COUNT(*) FROM courses"));
            stats.put("students", count(c, "SELECT COUNT(*) FROM users WHERE role = 'STUDENT'"));
            stats.put("enrollments", count(c, "SELECT COUNT(*) FROM enrollments"));
        }
        return stats;
    }

    private static int count(Connection c, String sql, int... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
