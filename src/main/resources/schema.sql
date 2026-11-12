-- Database schema (MySQL). Run automatically at application start-up.
-- Every statement uses IF NOT EXISTS, so it is safe to run repeatedly.

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(10)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS courses (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL UNIQUE,
    title       VARCHAR(150) NOT NULL,
    instructor  VARCHAR(100) NOT NULL,
    credits     INT          NOT NULL,
    seats       INT          NOT NULL,
    description VARCHAR(500)
);

-- One row per (student, course). The UNIQUE key stops double enrolment at the database level.
CREATE TABLE IF NOT EXISTS enrollments (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT       NOT NULL,
    course_id   INT       NOT NULL,
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, course_id),
    FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);
