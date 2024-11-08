-- Create the database
CREATE DATABASE IF NOT EXISTS library_system;
USE library_system;

-- Create tables with correct data types matching JPA entities
CREATE TABLE books (
                       id VARCHAR(255) PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       published_date VARCHAR(50),
                       description LONGTEXT,
                       average_rating DECIMAL(3,2),
                       ratings_count INT,
                       thumbnail_url VARCHAR(255),
                       copies_owned INT NOT NULL DEFAULT 1,
                       copies_available INT NOT NULL DEFAULT 1,
                       policy_type VARCHAR(50) NOT NULL DEFAULT 'BOOK'
);

CREATE TABLE authors (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE book_authors (
                              book_id VARCHAR(255),
                              author_id BIGINT,
                              PRIMARY KEY (book_id, author_id),
                              FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                              FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

CREATE TABLE categories (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE book_categories (
                                 book_id VARCHAR(255),
                                 category_id BIGINT,
                                 PRIMARY KEY (book_id, category_id),
                                 FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                                 FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE TABLE industry_identifiers (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      book_id VARCHAR(255),
                                      type VARCHAR(50),
                                      identifier VARCHAR(255),
                                      FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       role ENUM('MEMBER', 'LIBRARIAN', 'ADMIN') NOT NULL DEFAULT 'MEMBER',
                       status ENUM('ACTIVE', 'SUSPENDED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_policies (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               item_type VARCHAR(50) NOT NULL,
                               loan_period_days INT NOT NULL,
                               max_renewals INT NOT NULL,
                               grace_period_days INT DEFAULT 0,
                               reminder_days JSON NOT NULL COMMENT 'Array of days before due date to send reminders',
                               description VARCHAR(255),
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               UNIQUE KEY uk_item_type (item_type)
);

CREATE TABLE loans (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       book_id VARCHAR(255),
                       user_id BIGINT,
                       loan_date DATE NOT NULL,
                       due_date DATE NOT NULL,
                       return_date DATE,
                       renewal_count INT DEFAULT 0,
                       renewal_due_date DATE,
                       renewal_reason VARCHAR(255),
                       last_reminder_sent TIMESTAMP,
                       loan_policy_id BIGINT,
                       FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT,
                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
                       FOREIGN KEY (loan_policy_id) REFERENCES loan_policies(id) ON DELETE RESTRICT
);

CREATE TABLE loan_renewals (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               loan_id BIGINT NOT NULL,
                               renewal_date TIMESTAMP NOT NULL,
                               previous_due_date DATE NOT NULL,
                               new_due_date DATE NOT NULL,
                               reason VARCHAR(255),
                               created_by BIGINT NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
                               FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE reservations (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              book_id VARCHAR(255),
                              user_id BIGINT,
                              reservation_date DATETIME NOT NULL,
                              expiration_date DATETIME NOT NULL,
                              status ENUM('ACTIVE', 'FULFILLED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
                              FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE fines (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       loan_id BIGINT,
                       amount DECIMAL(10,2) NOT NULL,
                       reason VARCHAR(255) NOT NULL,
                       date_issued DATE NOT NULL,
                       date_paid DATE,
                       FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE RESTRICT
);

-- Create essential indexes
CREATE INDEX idx_book_title ON books(title);
CREATE INDEX idx_book_policy_type ON books(policy_type);
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_loan_return_date ON loans(return_date);
CREATE INDEX idx_loan_renewal_due_date ON loans(renewal_due_date);
CREATE INDEX idx_loan_last_reminder ON loans(last_reminder_sent);
CREATE INDEX idx_loan_renewals_date ON loan_renewals(renewal_date);
CREATE INDEX idx_reservation_status ON reservations(status);

-- Insert initial loan policies with descriptions
INSERT INTO loan_policies
(item_type, loan_period_days, max_renewals, grace_period_days, reminder_days, description)
VALUES
    ('BOOK', 14, 2, 2, '[7, 3, 1]', 'Standard books with normal loan period'),
    ('NEW_BOOK', 7, 1, 1, '[3, 1]', 'New releases with shorter loan period and limited renewals'),
    ('REFERENCE', 3, 0, 0, '[1]', 'Reference materials with very short loan period and no renewals'),
    ('PERIODICAL', 7, 1, 1, '[3, 1]', 'Magazines and journals with weekly loan period'),
    ('MEDIA', 7, 1, 1, '[3, 1]', 'DVDs, CDs, and other media items'),
    ('TEXTBOOK', 30, 3, 3, '[14, 7, 3]', 'Academic textbooks with extended loan period'),
    ('RESEARCH', 21, 2, 2, '[7, 3, 1]', 'Research materials with extended loan period');

-- Insert admin user (password: 'password123')
INSERT INTO users (
    username,
    password,
    email,
    first_name,
    last_name,
    role,
    status
) VALUES (
     'admin',
     '$2a$10$eQxk1RRedbDChD8.siG5Luh7PfYL7Qtt66tIcv2Erug/nNCzOl7kO',
     'admin@library.com',
     'System',
     'Admin',
     'ADMIN',
     'ACTIVE'
 );

-- Insert librarian (password: 'password123')
INSERT INTO users (
    username,
    password,
    email,
    first_name,
    last_name,
    role,
    status
) VALUES (
     'librarian',
     '$2a$10$eQxk1RRedbDChD8.siG5Luh7PfYL7Qtt66tIcv2Erug/nNCzOl7kO',
     'librarian@library.com',
     'Librarian',
     'One',
     'LIBRARIAN',
     'ACTIVE'
 );

-- Insert basic member (password: 'password123')
INSERT INTO users (
    username,
    password,
    email,
    first_name,
    last_name,
    role,
    status
) VALUES (
     'member',
     '$2a$10$eQxk1RRedbDChD8.siG5Luh7PfYL7Qtt66tIcv2Erug/nNCzOl7kO',
     'member@library.com',
     'Member',
     'One',
     'MEMBER',
     'ACTIVE'
 );

-- Insert basic member (password: 'password123')
INSERT INTO users (
    username,
    password,
    email,
    first_name,
    last_name,
    role,
    status
) VALUES (
     'member2',
     '$2a$10$eQxk1RRedbDChD8.siG5Luh7PfYL7Qtt66tIcv2Erug/nNCzOl7kO',
     'member2@library.com',
     'Member',
     'Two',
     'MEMBER',
     'ACTIVE'
 );