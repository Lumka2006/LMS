package com.lms.service;

import com.lms.model.Student;
import com.lms.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentService {
    
    public List<Student> getStudentsByInstructor(int instructorId) {
        List<Student> students = new ArrayList<>();
        String query = "SELECT DISTINCT u.* FROM users u " +
                      "JOIN enrollments e ON u.user_id = e.student_id " +
                      "JOIN courses c ON e.course_id = c.course_id " +
                      "WHERE c.teacher_id = ? AND u.role = 'STUDENT'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Student student = new Student(
                    rs.getInt("user_id"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("email")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching students for instructor " + instructorId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return students;
    }
    
    public List<Student> getStudentsByCourse(int courseId) {
        List<Student> students = new ArrayList<>();
        String query = "SELECT u.* FROM users u " +
                      "JOIN enrollments e ON u.user_id = e.student_id " +
                      "WHERE e.course_id = ? AND e.status = 'ACTIVE' AND u.role = 'STUDENT'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Student student = new Student(
                    rs.getInt("user_id"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("email")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching students for course " + courseId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return students;
    }
    
    public boolean enrollStudent(int studentId, int courseId) {
        String query = "INSERT INTO enrollments (student_id, course_id, status) VALUES (?, ?, 'ACTIVE')";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error enrolling student " + studentId + " in course " + courseId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean unenrollStudent(int studentId, int courseId) {
        String query = "UPDATE enrollments SET status = 'DROPPED' WHERE student_id = ? AND course_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error unenrolling student " + studentId + " from course " + courseId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 