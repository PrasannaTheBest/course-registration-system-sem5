package com.course.dao;

import com.course.model.Course;
import com.course.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {
    static final String SELECT_WITH_COUNT =
            "SELECT c.id, c.code, c.title, c.instructor, c.credits, c.seats, c.description, "
                    + "(SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.id) AS enrolled "
                    + "FROM courses c";

    /** Lists courses, optionally filtered by a search term on code, title or instructor. */
    public List<Course> search(String term) throws SQLException {
        boolean filtered = term != null && !term.trim().isEmpty();
        String sql = SELECT_WITH_COUNT
                + (filtered ? " WHERE LOWER(c.code) LIKE ? OR LOWER(c.title) LIKE ? OR LOWER(c.instructor) LIKE ?" : "")
                + " ORDER BY c.code";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (filtered) {
                String like = "%" + term.trim().toLowerCase() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }
            List<Course> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        }
    }

    public Course findById(int id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_WITH_COUNT + " WHERE c.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Inserts (id == 0) or updates a course.
     * @return false if another course already uses the same code.
     */
    public boolean save(Course course) throws SQLException {
        boolean isNew = course.getId() == 0;
        String sql = isNew
                ? "INSERT INTO courses (code, title, instructor, credits, seats, description) VALUES (?,?,?,?,?,?)"
                : "UPDATE courses SET code=?, title=?, instructor=?, credits=?, seats=?, description=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, course.getCode());
            ps.setString(2, course.getTitle());
            ps.setString(3, course.getInstructor());
            ps.setInt(4, course.getCredits());
            ps.setInt(5, course.getSeats());
            ps.setString(6, course.getDescription());
            if (!isNew) {
                ps.setInt(7, course.getId());
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                return false; // duplicate course code
            }
            throw e;
        }
    }

    /** Deletes a course; its enrolments are removed too (ON DELETE CASCADE). */
    public void delete(int id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM courses WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM courses");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    static Course map(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setTitle(rs.getString("title"));
        c.setInstructor(rs.getString("instructor"));
        c.setCredits(rs.getInt("credits"));
        c.setSeats(rs.getInt("seats"));
        c.setDescription(rs.getString("description"));
        c.setEnrolled(rs.getInt("enrolled"));
        return c;
    }
}
