# Online Course Registration System

Project #18 from the PBLJ (Project Based Learning in Java) list.
Built with **Java, Servlets and JSP**, using JDBC with MySQL and Maven.

Students register, browse courses, enrol (with seat limits) and drop courses.
Admins add, edit and delete courses and see who is enrolled in each one.

## Requirements

- JDK 11 or newer
- Maven 3.6+
- MySQL 8 (or use the no-install demo mode below)
- *Optional:* Apache Tomcat **9** (not 10 or 11, which use `jakarta.*` instead of `javax.*`)

## Run it

### Option 1: with MySQL (the real setup)

1. Start MySQL and open `src/main/resources/db.properties`. Set `db.user` and `db.password` to yours.
   The database `course_db`, all tables, an admin account and 6 sample courses are created automatically.
2. Start the app:
   ```
   mvn jetty:run
   ```
3. Open <http://localhost:8080/>

### Option 2: demo mode (no MySQL needed)

Uses an embedded H2 database stored in `./demo-data`:
```
mvn jetty:run -Pdemo
```

### Option 3: deploy to Tomcat 9

```
mvn clean package
```
Copy `target/course-registration.war` into Tomcat's `webapps/` folder, start Tomcat, and open
<http://localhost:8080/course-registration/>

## Logins

| Role    | Email               | Password   |
|---------|---------------------|------------|
| Admin   | admin@college.edu   | admin123   |
| Student | click **Register**  | your own   |

## Features

**Student**
- Register and log in (passwords stored as salted PBKDF2 hashes)
- Search courses by code, title or instructor
- Enrol in a course; a full course cannot be joined, a course cannot be joined twice
- "My Courses" page with enrolment date and total credits; drop a course

**Admin**
- Dashboard with totals (courses, students, enrolments)
- Add, edit and delete courses (seats cannot be set below the number already enrolled)
- See the list of students enrolled in each course

## Project structure

```
src/main/java/com/course
  model/      User, Course, Enrollment             (plain Java beans)
  dao/        UserDAO, CourseDAO, EnrollmentDAO    (all JDBC / SQL lives here)
  servlet/    Login, Register, Logout
    student/  CourseList, Enroll, Drop, MyCourses
    admin/    AdminCourses, CourseForm, CourseDelete, CourseStudents
  filter/     AuthFilter (login + role check), EncodingFilter (UTF-8)
  listener/   AppInitListener (creates tables, seeds data at start-up)
  util/       DBUtil, PasswordUtil, Flash
src/main/webapp
  WEB-INF/views/   JSP pages (hidden from direct URL access)
  css/style.css
src/main/resources  schema.sql, db.properties
```

Flow: browser, then Servlet (controller), then DAO (JDBC), then request attribute, then JSP (view). This is MVC.

## Viva cheat-sheet: where each concept appears

| Concept | Where |
|---|---|
| Servlet lifecycle, `doGet` / `doPost` | every class in `servlet/` |
| `@WebServlet`, `@WebFilter`, `@WebListener` | servlets, `AuthFilter`, `AppInitListener` |
| Request forward vs redirect | forward to JSP on errors; redirect after a successful POST (Post/Redirect/Get) |
| Session tracking | `LoginServlet` stores the user in `HttpSession`; `Flash` messages |
| Filters | `AuthFilter` protects `/student/*` and `/admin/*` |
| ServletContextListener | `AppInitListener` creates the schema once at start-up |
| JSP, JSTL, EL | `c:forEach`, `c:if`, `c:out`, `${...}` in every view; static `include` for header/footer |
| JDBC, PreparedStatement | all DAOs (parameters prevent SQL injection) |
| Transactions | `EnrollmentDAO.enroll`: `setAutoCommit(false)`, `SELECT ... FOR UPDATE`, commit/rollback |
| Security | PBKDF2 password hashing, `c:out` escaping (XSS), session id renewed at login, role check |
| Error handling | `error-page` entries in `web.xml`, `error.jsp` |

## Tests

```
mvn test
```
Runs the DAO logic (enrol, drop, full course, duplicates, cascade delete, search) against an in-memory H2 database.

## Ideas for extra marks

- CSRF token on every POST form
- Prerequisites per course, or a credit limit per student
- Waiting list when a course is full
- Export "my courses" or the student list as PDF/CSV
- Pagination on the course list
