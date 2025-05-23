package com.lms.service;

import com.lms.model.Submission;
import com.lms.model.SubmissionAttachment;
import com.lms.model.Student;
import com.lms.model.User;
import com.lms.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubmissionService {
    
    public List<Submission> getSubmissionsByAssignment(int assignmentId) throws SQLException {
        List<Submission> submissions = new ArrayList<>();
        String query = "SELECT s.*, u.first_name, u.last_name, u.username, u.email, u.password, u.role " +
                      "FROM submissions s " +
                      "JOIN users u ON s.student_id = u.user_id " +
                      "WHERE s.assignment_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, assignmentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                User student = new User(
                    rs.getInt("student_id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("role")
                );
                
                Submission submission = new Submission(
                    rs.getInt("submission_id"),
                    student,
                    null, // Assignment will be set later if needed
                    rs.getString("content")
                );
                
                // Set additional properties
                submission.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
                submission.setScore(rs.getDouble("grade"));
                submission.setFeedback(rs.getString("feedback"));
                
                submissions.add(submission);
            }
        }
        return submissions;
    }
    
    public List<SubmissionAttachment> getSubmissionAttachments(int submissionId) throws SQLException {
        List<SubmissionAttachment> attachments = new ArrayList<>();
        String query = "SELECT * FROM submission_attachments WHERE submission_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, submissionId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                SubmissionAttachment attachment = new SubmissionAttachment(
                    rs.getInt("attachment_id"),
                    rs.getInt("submission_id"),
                    rs.getString("file_name"),
                    rs.getString("file_path"),
                    rs.getString("file_type"),
                    rs.getLong("file_size")
                );
                attachments.add(attachment);
            }
        }
        return attachments;
    }
    
    public boolean updateSubmission(Submission submission) throws SQLException {
        String query = "UPDATE submissions SET grade = ?, feedback = ?, status = ? WHERE submission_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDouble(1, submission.getScore());
            pstmt.setString(2, submission.getFeedback());
            pstmt.setString(3, submission.isGraded() ? "GRADED" : "SUBMITTED");
            pstmt.setInt(4, submission.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
} 