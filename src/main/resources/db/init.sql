-- Drop existing tables if they exist
DROP TABLE IF EXISTS submission_attachments;
DROP TABLE IF EXISTS submissions;

-- Create submissions table
CREATE TABLE IF NOT EXISTS submissions (
    submission_id SERIAL PRIMARY KEY,
    assignment_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    content TEXT,
    submitted_at TIMESTAMP NOT NULL,
    grade DECIMAL(5,2),
    feedback TEXT,
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id),
    FOREIGN KEY (student_id) REFERENCES users(user_id)
);

-- Create submission attachments table
CREATE TABLE IF NOT EXISTS submission_attachments (
    attachment_id SERIAL PRIMARY KEY,
    submission_id INTEGER NOT NULL,
    file_path TEXT NOT NULL,
    file_name TEXT NOT NULL,
    file_type TEXT,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submissions(submission_id)
); 