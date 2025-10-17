# Library Management System

## Overview

This is a desktop-based **Library Management System** developed using **Java Swing** for the graphical user interface and **JDBC** for MySQL database connectivity. It allows users to add, search, borrow, and return books efficiently.

---

## Features

- Add new books with title and author details
- View all books with current availability status
- Search books by title or author
- Borrow and return books with status updates
- User-friendly Swing GUI for easy interaction
- Persistent storage using MySQL database

---

## Technologies Used

- Java SE (Swing for GUI)
- JDBC API for database connectivity
- MySQL for relational database management

---

## Database Setup

Execute the following SQL commands to create the database and table:

```sql
CREATE DATABASE librarydb;

USE librarydb;

CREATE TABLE books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    is_borrowed BOOLEAN DEFAULT FALSE
);
