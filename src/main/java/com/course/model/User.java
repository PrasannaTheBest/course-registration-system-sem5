package com.course.model;

public class User {
    public static final String ADMIN = "ADMIN";
    public static final String STUDENT = "STUDENT";

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String role;

    public User() {
    }

    public User(int id, String name, String email, String passwordHash, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public boolean isAdmin() { return ADMIN.equals(role); }
}
