CREATE DATABASE librarydb;

USE librarydb;

CREATE TABLE books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    is_borrowed BOOLEAN DEFAULT FALSE
);
