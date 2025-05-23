package com.lms.service;

import com.lms.model.Assignment;
import com.lms.model.Submission;
import com.lms.model.User;
import com.lms.util.DatabaseUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssignmentService {
    
    public List<Assignment> getAssignmentsByInstructor(int instructorId) {
        List<Assignment> assignments = new ArrayList<>();
        String query = "SELECT a.*, c.course_code, c.title as course_name FROM assignments a " +
                      "JOIN courses c ON a.course_id = c.course_id " +
                      "WHERE c.teacher_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                java.sql.Timestamp timestamp = rs.getTimestamp("due_date");
                LocalDateTime dueDate = timestamp != null ? timestamp.toLocalDateTime() : null;
                
                Assignment assignment = new Assignment(
                    rs.getInt("assignment_id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    dueDate,
                    rs.getInt("total_points"),
                    rs.getString("course_name")
                );
                assignments.add(assignment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return assignments;
    }
    
    public boolean addAssignment(Assignment assignment) {
        String query = "INSERT INTO assignments (course_id, title, description, due_date, total_points) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, assignment.getCourseId());
            pstmt.setString(2, assignment.getTitle());
            pstmt.setString(3, assignment.getDescription());
            pstmt.setTimestamp(4, assignment.getDueDate() != null ? 
                java.sql.Timestamp.valueOf(assignment.getDueDate()) : null);
            pstmt.setInt(5, assignment.getTotalPoints());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateAssignment(Assignment assignment) {
        String query = "UPDATE assignments SET title = ?, description = ?, " +
                      "due_date = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, assignment.getTitle());
            pstmt.setString(2, assignment.getDescription());
            pstmt.setTimestamp(3, assignment.getDueDate() != null ? 
                java.sql.Timestamp.valueOf(assignment.getDueDate()) : null);
            pstmt.setInt(4, assignment.getAssignmentId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteAssignment(int assignmentId) {
        String query = "DELETE FROM assignments WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, assignmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean submitAssignment(Assignment assignment, User student, String content, List<String> attachments) {
        String query = "INSERT INTO submissions (assignment_id, student_id, content, submitted_at) " +
                      "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, assignment.getAssignmentId());
            pstmt.setInt(2, student.getUserId());
            pstmt.setString(3, content);
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                // Get the generated submission ID
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int submissionId = rs.getInt(1);
                        // Save attachments if any
                        if (attachments != null && !attachments.isEmpty()) {
                            saveAttachments(submissionId, attachments);
                        }
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveAttachments(int submissionId, List<String> attachments) throws SQLException {
        String query = "INSERT INTO submission_attachments (submission_id, file_path) VALUES (?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            for (String filePath : attachments) {
                pstmt.setInt(1, submissionId);
                pstmt.setString(2, filePath);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public Submission getSubmission(Assignment assignment, User student) {
        String query = "SELECT s.*, GROUP_CONCAT(sa.file_path) as attachments " +
                      "FROM submissions s " +
                      "LEFT JOIN submission_attachments sa ON s.id = sa.submission_id " +
                      "WHERE s.assignment_id = ? AND s.student_id = ? " +
                      "GROUP BY s.id";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, assignment.getAssignmentId());
            pstmt.setInt(2, student.getUserId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Submission submission = new Submission(
                        rs.getInt("id"),
                        assignment.getAssignmentId(),
                        student.getUserId(),
                        student.getName(),
                        rs.getString("content"),
                        rs.getDouble("score")
                    );
                    
                    // Set submission time
                    java.sql.Timestamp submittedAt = rs.getTimestamp("submitted_at");
                    if (submittedAt != null) {
                        submission.setSubmittedAt(submittedAt.toLocalDateTime());
                    }
                    
                    // Set feedback if any
                    String feedback = rs.getString("feedback");
                    if (feedback != null) {
                        submission.setFeedback(feedback);
                    }
                    
                    // Add attachments if any
                    String attachments = rs.getString("attachments");
                    if (attachments != null) {
                        for (String filePath : attachments.split(",")) {
                            submission.addAttachment(filePath.trim());
                        }
                    }
                    
                    return submission;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isSubmitted(Assignment assignment, User student) {
        String query = "SELECT COUNT(*) FROM submissions WHERE assignment_id = ? AND student_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, assignment.getAssignmentId());
            pstmt.setInt(2, student.getUserId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateSubmission(Submission submission) {
        String query = "UPDATE submissions SET content = ?, score = ?, feedback = ?, " +
                      "graded_at = ?, graded_by = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, submission.getContent());
            pstmt.setDouble(2, submission.getScore());
            pstmt.setString(3, submission.getFeedback());
            pstmt.setTimestamp(4, submission.getGradedAt() != null ? 
                java.sql.Timestamp.valueOf(submission.getGradedAt()) : null);
            pstmt.setInt(5, submission.getGradedBy() != null ? 
                submission.getGradedBy().getUserId() : 0);
            pstmt.setInt(6, submission.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 