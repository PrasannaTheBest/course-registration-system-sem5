package com.course.dao;

import com.course.model.User;
import com.course.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email, password_hash, role FROM users WHERE email = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"),
                            rs.getString("password_hash"), rs.getString("role"));
                }
                return null;
            }
        }
    }

    /** @return false if the email is already registered. */
    public boolean create(String name, String email, String passwordHash, String role) throws SQLException {
        if (findByEmail(email) != null) {
            return false;
        }
        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email.toLowerCase());
            ps.setString(3, passwordHash);
            ps.setString(4, role);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                return false; // unique-key violation from a concurrent registration
            }
            throw e;
        }
    }

    public int count() throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
