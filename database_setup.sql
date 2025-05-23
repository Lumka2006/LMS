-- Create the database
CREATE DATABASE learningmanagementsystem;

-- Connect to the database
\c learningmanagementsystem;

-- Create Users table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Courses table
CREATE TABLE courses (
    course_id SERIAL PRIMARY KEY,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    teacher_id INTEGER REFERENCES users(user_id),
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Enrollments table
CREATE TABLE enrollments (
    enrollment_id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES users(user_id),
    course_id INTEGER REFERENCES courses(course_id),
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'DROPPED')),
    UNIQUE(student_id, course_id)
);

-- Create Assignments table
CREATE TABLE assignments (
    assignment_id SERIAL PRIMARY KEY,
    course_id INTEGER REFERENCES courses(course_id),
    title VARCHAR(100) NOT NULL,
    description TEXT,
    due_date TIMESTAMP NOT NULL,
    total_points INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Submissions table
CREATE TABLE submissions (
    submission_id SERIAL PRIMARY KEY,
    assignment_id INTEGER REFERENCES assignments(assignment_id),
    student_id INTEGER REFERENCES users(user_id),
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    grade INTEGER,
    feedback TEXT,
    status VARCHAR(20) DEFAULT 'SUBMITTED' CHECK (status IN ('SUBMITTED', 'GRADED', 'LATE')),
    UNIQUE(assignment_id, student_id)
);

-- Create Announcements table
CREATE TABLE announcements (
    announcement_id SERIAL PRIMARY KEY,
    course_id INTEGER REFERENCES courses(course_id),
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_by INTEGER REFERENCES users(user_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_courses_teacher ON courses(teacher_id);
CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_enrollments_course ON enrollments(course_id);
CREATE INDEX idx_assignments_course ON assignments(course_id);
CREATE INDEX idx_submissions_assignment ON submissions(assignment_id);
CREATE INDEX idx_submissions_student ON submissions(student_id);
CREATE INDEX idx_announcements_course ON announcements(course_id);

-- Insert default admin user
INSERT INTO users (username, password, email, first_name, last_name, role)
VALUES ('admin', '$2a$10$rDkPvvAFV6GgJkKq8K6UxO9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X', 'admin@lms.com', 'Admin', 'User', 'ADMIN'); 