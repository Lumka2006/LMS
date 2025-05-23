package com.lms.model;

public class SubmissionAttachment {
    private int attachmentId;
    private int submissionId;
    private String fileName;
    private String filePath;
    private String fileType;
    private long fileSize;

    public SubmissionAttachment(int attachmentId, int submissionId, String fileName, 
                               String filePath, String fileType, long fileSize) {
        this.attachmentId = attachmentId;
        this.submissionId = submissionId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    // Getters and setters
    public int getAttachmentId() { return attachmentId; }
    public void setAttachmentId(int attachmentId) { this.attachmentId = attachmentId; }
    
    public int getSubmissionId() { return submissionId; }
    public void setSubmissionId(int submissionId) { this.submissionId = submissionId; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
} 