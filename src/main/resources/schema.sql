-- 1. Create and Select the Database
CREATE DATABASE IF NOT EXISTS educational_platform;
USE educational_platform;

-- 2. Subject Table
CREATE TABLE IF NOT EXISTS `subject` (
    `id` VARCHAR(36) PRIMARY KEY,
    `name` VARCHAR(255),
    `code` VARCHAR(50),
    `description` TEXT
);

-- 3. Class Table
CREATE TABLE IF NOT EXISTS `class` (
    `id` VARCHAR(36) PRIMARY KEY,
    `name` VARCHAR(255),
    `teacherId` VARCHAR(36)
);

-- 4. Junction Table for Class and Subjects
CREATE TABLE IF NOT EXISTS `class_subjects` (
    `class_id` VARCHAR(36),
    `subject_id` VARCHAR(36),
    PRIMARY KEY (`class_id`, `subject_id`),
    FOREIGN KEY (`class_id`) REFERENCES `class`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`subject_id`) REFERENCES `subject`(`id`) ON DELETE CASCADE
);

-- 5. User Table
CREATE TABLE IF NOT EXISTS `User` (
    `id` VARCHAR(36) PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(50) DEFAULT 'student',
    `status` VARCHAR(50) DEFAULT 'active',
    `class_id` VARCHAR(36),
    `teacher_id` VARCHAR(36),
    FOREIGN KEY (`class_id`) REFERENCES `class`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`teacher_id`) REFERENCES `User`(`id`) ON DELETE SET NULL
);

-- 6. Announcement Table
CREATE TABLE IF NOT EXISTS `announcement` (
    `id` VARCHAR(36) PRIMARY KEY,
    `title` VARCHAR(255),
    `content` TEXT,
    `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `targetRole` VARCHAR(50)
);

-- 7. Assignment Table
CREATE TABLE IF NOT EXISTS `assignment` (
    `id` VARCHAR(36) PRIMARY KEY,
    `title` VARCHAR(255),
    `description` TEXT,
    `deadline` DATETIME,
    `fileUrl` VARCHAR(255),
    `class_id` VARCHAR(36),
    FOREIGN KEY (`class_id`) REFERENCES `class`(`id`) ON DELETE CASCADE
);

-- 8. Submission Table
CREATE TABLE IF NOT EXISTS `submission` (
    `id` VARCHAR(36) PRIMARY KEY,
    `submittedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `fileUrl` VARCHAR(255),
    `grade` INT,
    `feedback` TEXT,
    `status` VARCHAR(50) DEFAULT 'submitted',
    `assignment_id` VARCHAR(36),
    `student_id` VARCHAR(36),
    FOREIGN KEY (`assignment_id`) REFERENCES `assignment`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `User`(`id`) ON DELETE CASCADE
);

-- 9. Mark Table
CREATE TABLE IF NOT EXISTS `mark` (
    `id` VARCHAR(36) PRIMARY KEY,
    `marksObtained` INT,
    `maxMarks` INT,
    `examType` VARCHAR(100),
    `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `student_id` VARCHAR(36),
    `subject_id` VARCHAR(36),
    FOREIGN KEY (`student_id`) REFERENCES `User`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`subject_id`) REFERENCES `subject`(`id`) ON DELETE CASCADE
);

-- 9b. Password Reset Token Table
CREATE TABLE IF NOT EXISTS `password_reset_token` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NOT NULL,
    `otp` VARCHAR(10) NOT NULL,
    `expiry_time` DATETIME NOT NULL
);

-- 10. Insert Admin User (Password is 'admin')
INSERT IGNORE INTO `User` (`id`, `name`, `email`, `password`, `role`, `status`) 
VALUES (UUID(), 'Administrator', 'admin@gmail.com', 'admin', 'admin', 'active');
