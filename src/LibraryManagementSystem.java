import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LibraryManagementSystem extends JFrame {

    private JTextField txtTitle, txtAuthor, txtSearch;
    private JTable table;
    private DefaultTableModel model;

    public LibraryManagementSystem() {
        setTitle("Library Management System");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel for adding books
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Book"));

        txtTitle = new JTextField();
        txtAuthor = new JTextField();
        JButton btnAdd = new JButton("Add Book");

        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(txtTitle);
        inputPanel.add(new JLabel("Author:"));
        inputPanel.add(txtAuthor);
        inputPanel.add(new JLabel(""));
        inputPanel.add(btnAdd);

        add(inputPanel, BorderLayout.NORTH);

        // Center panel for table and search
        model = new DefaultTableModel(new String[]{"Book ID", "Title", "Author", "Status"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Books"));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel for borrow/return
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton btnBorrow = new JButton("Borrow Book");
        JButton btnReturn = new JButton("Return Book");

        bottomPanel.add(btnBorrow);
        bottomPanel.add(btnReturn);

        add(bottomPanel, BorderLayout.SOUTH);

        // Load data initially
        loadBooks();

        // Button actions
        btnAdd.addActionListener(e -> addBook());
        btnSearch.addActionListener(e -> searchBooks());
        btnRefresh.addActionListener(e -> loadBooks());
        btnBorrow.addActionListener(e -> borrowBook());
        btnReturn.addActionListener(e -> returnBook());
    }

    private void loadBooks() {
        model.setRowCount(0); // Clear existing rows
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                int id = rs.getInt("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                boolean isBorrowed = rs.getBoolean("is_borrowed");
                model.addRow(new Object[]{id, title, author, isBorrowed ? "Borrowed" : "Available"});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage());
        }
    }

    private void addBook() {
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both title and author.");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title, author) VALUES (?, ?)")) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Book added successfully.");
            txtTitle.setText("");
            txtAuthor.setText("");
            loadBooks();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding book: " + ex.getMessage());
        }
    }

    private void searchBooks() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadBooks();
            return;
        }

        model.setRowCount(0);
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                boolean isBorrowed = rs.getBoolean("is_borrowed");
                model.addRow(new Object[]{id, title, author, isBorrowed ? "Borrowed" : "Available"});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching books: " + ex.getMessage());
        }
    }

    private void borrowBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to borrow.");
            return;
        }

        int bookId = (int) model.getValueAt(selectedRow, 0);
        String status = (String) model.getValueAt(selectedRow, 3);

        if ("Borrowed".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Book is already borrowed.");
            return;
        }

        updateBookBorrowStatus(bookId, true);
    }

    private void returnBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to return.");
            return;
        }

        int bookId = (int) model.getValueAt(selectedRow, 0);
        String status = (String) model.getValueAt(selectedRow, 3);

        if ("Available".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Book is not borrowed.");
            return;
        }

        updateBookBorrowStatus(bookId, false);
    }

    private void updateBookBorrowStatus(int bookId, boolean borrow) {
        String sql = "UPDATE books SET is_borrowed = ? WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, borrow);
            ps.setInt(2, bookId);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this, borrow ? "Book borrowed successfully." : "Book returned successfully.");
                loadBooks();
            } else {
                JOptionPane.showMessageDialog(this, "Book not found.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating book status: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LibraryManagementSystem().setVisible(true);
        });
    }
}
