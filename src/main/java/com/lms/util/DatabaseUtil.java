package com.lms.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.lms.model.User;
import java.time.LocalDateTime;
import java.sql.Timestamp;

public class DatabaseUtil {
    private static final String URL = "jdbc:postgresql://localhost:5432/learningmanagementsystem";
    private static final String USER = "postgres";
    private static final String PASSWORD = "LumkaMdandy@2006";
    
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found.", e);
        }
    }
    
    private static User currentUser;
    
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                // Get database metadata
                DatabaseMetaData metaData = conn.getMetaData();
                String dbName = metaData.getDatabaseProductName();
                String dbVersion = metaData.getDatabaseProductVersion();
                
                System.out.println("Database connection successful!");
                System.out.println("Connected to: " + URL);
                System.out.println("Database: " + dbName + " " + dbVersion);
                System.out.println("User: " + USER);
                
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e);
        }
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id SERIAL PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    first_name VARCHAR(50) NOT NULL,
                    last_name VARCHAR(50) NOT NULL,
                    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create courses table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    course_id SERIAL PRIMARY KEY,
                    course_code VARCHAR(20) UNIQUE NOT NULL,
                    title VARCHAR(100) NOT NULL,
                    description TEXT,
                    teacher_id INTEGER REFERENCES users(user_id),
                    start_date DATE,
                    end_date DATE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create enrollments table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS enrollments (
                    enrollment_id SERIAL PRIMARY KEY,
                    student_id INTEGER REFERENCES users(user_id),
                    course_id INTEGER REFERENCES courses(course_id),
                    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'DROPPED')),
                    UNIQUE(student_id, course_id)
                )
            """);

            // Create assignments table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS assignments (
                    assignment_id SERIAL PRIMARY KEY,
                    course_id INTEGER REFERENCES courses(course_id),
                    title VARCHAR(100) NOT NULL,
                    description TEXT,
                    due_date TIMESTAMP NOT NULL,
                    total_points INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create submissions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS submissions (
                    submission_id SERIAL PRIMARY KEY,
                    assignment_id INTEGER REFERENCES assignments(assignment_id),
                    student_id INTEGER REFERENCES users(user_id),
                    content TEXT,
                    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    grade INTEGER,
                    feedback TEXT,
                    status VARCHAR(20) DEFAULT 'SUBMITTED' CHECK (status IN ('SUBMITTED', 'GRADED', 'LATE')),
                    UNIQUE(assignment_id, student_id)
                )
            """);

            // Create submission attachments table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS submission_attachments (
                    attachment_id SERIAL PRIMARY KEY,
                    submission_id INTEGER REFERENCES submissions(submission_id),
                    file_path TEXT NOT NULL,
                    file_name TEXT NOT NULL,
                    file_type TEXT,
                    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create announcements table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS announcements (
                    announcement_id SERIAL PRIMARY KEY,
                    course_id INTEGER REFERENCES courses(course_id),
                    title VARCHAR(100) NOT NULL,
                    content TEXT NOT NULL,
                    created_by INTEGER REFERENCES users(user_id),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create indexes for better performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_courses_teacher ON courses(teacher_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_enrollments_student ON enrollments(student_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_enrollments_course ON enrollments(course_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_assignments_course ON assignments(course_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_submissions_assignment ON submissions(assignment_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_submissions_student ON submissions(student_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_announcements_course ON announcements(course_id)");

            // Rename submission_date to submitted_at if it exists
            try {
                stmt.execute("ALTER TABLE submissions RENAME COLUMN submission_date TO submitted_at");
            } catch (SQLException e) {
                // Column might not exist or already be renamed, ignore the error
                if (!e.getMessage().contains("does not exist") && !e.getMessage().contains("already exists")) {
                    throw e;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // User operations
    public static boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("role")
                    );
                }
            }
        }
        return null;
    }

    public static void addUser(String username, String password, String email, String firstName, String lastName, String role) throws SQLException {
        String query = "INSERT INTO users (username, password, email, first_name, last_name, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, firstName);
            pstmt.setString(5, lastName);
            pstmt.setString(6, role);
            
            pstmt.executeUpdate();
        }
    }

    // Course operations
    public static List<String> getEnrolledCourses(String username) {
        List<String> courses = new ArrayList<>();
        String query = """
            SELECT c.title FROM courses c
            JOIN enrollments e ON c.course_id = e.course_id
            JOIN users u ON e.student_id = u.user_id
            WHERE u.username = ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(rs.getString("title"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return courses;
    }

    // Assignment operations
    public static List<String> getAssignmentsForCourse(String courseTitle) {
        List<String> assignments = new ArrayList<>();
        String query = """
            SELECT a.title FROM assignments a
            JOIN courses c ON a.course_id = c.course_id
            WHERE c.title = ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, courseTitle);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    assignments.add(rs.getString("title"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return assignments;
    }

    // Statistics methods
    public static int getTotalCourses() {
        String query = "SELECT COUNT(*) FROM courses";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTotalAssignments() {
        String query = "SELECT COUNT(*) FROM assignments";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getAverageProgress() {
        String query = """
            SELECT AVG(CASE 
                WHEN s.status = 'GRADED' THEN 1.0
                WHEN s.status = 'SUBMITTED' THEN 0.5
                ELSE 0.0
            END) as avg_progress
            FROM submissions s
        """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getDouble("avg_progress");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static void addCourse(String courseCode, String title, String description, 
                               int teacherId, Date startDate, Date endDate) throws SQLException {
        String query = "INSERT INTO courses (course_code, title, description, teacher_id, start_date, end_date) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, courseCode);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setInt(4, teacherId);
            pstmt.setDate(5, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(6, new java.sql.Date(endDate.getTime()));
            
            pstmt.executeUpdate();
        }
    }

    public static void addAssignment(int courseId, String title, String description, LocalDateTime dueDate, double totalPoints) throws SQLException {
        String query = "INSERT INTO assignments (course_id, title, description, due_date, total_points) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, courseId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setTimestamp(4, Timestamp.valueOf(dueDate));
            pstmt.setDouble(5, totalPoints);
            pstmt.executeUpdate();
        }
    }

    public static void checkEnrollments() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check users table
            System.out.println("\nChecking users table:");
            ResultSet usersRs = stmt.executeQuery("SELECT * FROM users");
            while (usersRs.next()) {
                System.out.println("User: " + usersRs.getString("username") + 
                                 " (ID: " + usersRs.getInt("user_id") + 
                                 ", Role: " + usersRs.getString("role") + ")");
            }
            
            // Check courses table
            System.out.println("\nChecking courses table:");
            ResultSet coursesRs = stmt.executeQuery("SELECT * FROM courses");
            while (coursesRs.next()) {
                System.out.println("Course: " + coursesRs.getString("title") + 
                                 " (ID: " + coursesRs.getInt("course_id") + 
                                 ", Code: " + coursesRs.getString("course_code") + ")");
            }
            
            // Check enrollments table
            System.out.println("\nChecking enrollments table:");
            ResultSet enrollmentsRs = stmt.executeQuery("SELECT e.*, u.username, c.title as course_title " +
                                                      "FROM enrollments e " +
                                                      "JOIN users u ON e.student_id = u.user_id " +
                                                      "JOIN courses c ON e.course_id = c.course_id");
            while (enrollmentsRs.next()) {
                System.out.println("Enrollment: Student " + enrollmentsRs.getString("username") + 
                                 " in course " + enrollmentsRs.getString("course_title") + 
                                 " (Status: " + enrollmentsRs.getString("status") + ")");
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking database: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 