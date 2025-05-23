package com.lms.service;

import com.lms.model.Course;
import com.lms.util.DatabaseUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CourseService {
    
    public List<Course> getCoursesByInstructor(int instructorId) {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT c.*, CONCAT(u.first_name, ' ', u.last_name) as instructor_name " +
                      "FROM courses c " +
                      "LEFT JOIN users u ON c.teacher_id = u.user_id " +
                      "WHERE c.teacher_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                java.sql.Timestamp startTimestamp = rs.getTimestamp("start_date");
                java.sql.Timestamp endTimestamp = rs.getTimestamp("end_date");
                
                LocalDateTime startDate = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
                LocalDateTime endDate = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;
                
                Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("teacher_id"),
                    rs.getString("instructor_name"),
                    startDate,
                    endDate
                );
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return courses;
    }
    
    public boolean addCourse(Course course) {
        String query = "INSERT INTO courses (course_code, title, description, teacher_id, start_date, end_date) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, course.getCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setString(3, course.getDescription());
            pstmt.setInt(4, course.getTeacherId());
            pstmt.setTimestamp(5, course.getStartDate() != null ? 
                java.sql.Timestamp.valueOf(course.getStartDate()) : null);
            pstmt.setTimestamp(6, course.getEndDate() != null ? 
                java.sql.Timestamp.valueOf(course.getEndDate()) : null);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateCourse(Course course) {
        String query = "UPDATE courses SET course_code = ?, title = ?, description = ?, " +
                      "start_date = ?, end_date = ? WHERE course_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, course.getCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setString(3, course.getDescription());
            pstmt.setTimestamp(4, course.getStartDate() != null ? 
                java.sql.Timestamp.valueOf(course.getStartDate()) : null);
            pstmt.setTimestamp(5, course.getEndDate() != null ? 
                java.sql.Timestamp.valueOf(course.getEndDate()) : null);
            pstmt.setInt(6, course.getCourseId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteCourse(int courseId) {
        String query = "DELETE FROM courses WHERE course_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, courseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Course> getAvailableCourses(int studentId) {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT c.*, CONCAT(u.first_name, ' ', u.last_name) as instructor_name " +
                      "FROM courses c " +
                      "LEFT JOIN users u ON c.teacher_id = u.user_id " +
                      "WHERE c.course_id NOT IN (SELECT course_id FROM enrollments WHERE student_id = ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                java.sql.Timestamp startTimestamp = rs.getTimestamp("start_date");
                java.sql.Timestamp endTimestamp = rs.getTimestamp("end_date");
                
                LocalDateTime startDate = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
                LocalDateTime endDate = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;
                
                Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("teacher_id"),
                    rs.getString("instructor_name"),
                    startDate,
                    endDate
                );
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return courses;
    }

    public List<Course> getCoursesByStudent(int studentId) {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT c.*, CONCAT(u.first_name, ' ', u.last_name) as instructor_name " +
                      "FROM courses c " +
                      "JOIN enrollments e ON c.course_id = e.course_id " +
                      "LEFT JOIN users u ON c.teacher_id = u.user_id " +
                      "WHERE e.student_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp startTimestamp = rs.getTimestamp("start_date");
                    java.sql.Timestamp endTimestamp = rs.getTimestamp("end_date");
                    
                    LocalDateTime startDate = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
                    LocalDateTime endDate = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;
                    
                    Course course = new Course(
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("teacher_id"),
                        rs.getString("instructor_name"),
                        startDate,
                        endDate
                    );
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }
} 