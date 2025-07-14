package com.library.library_system.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.library.library_system.model.Book;
import com.library.library_system.service.BookService;
import javafx.concurrent.Task;
import java.util.List;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;

/*
 * Controller for the Book Management View
 * Handles the management of books in the library system
 * Includes CRUD operations for books
 * 
 */

public class BookViewController {
    /*
     * FXML Elements
     */
    @FXML
    private Button bookNavButton;
    @FXML
    private TableView<Book> bookTable;
    @FXML
    private TableColumn<Book, Integer> bookIdColumn;
    @FXML
    private TableColumn<Book, String> isbnColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorsColumn;
    @FXML
    private TableColumn<Book, String> publisherColumn;
    @FXML
    private TableColumn<Book, Integer> pagesColumn;
    @FXML
    private TableColumn<Book, String> publicationDateColumn;
    @FXML
    private TableColumn<Book, String> descriptionColumn;
    @FXML
    private TextField isbnField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField authorsField;
    @FXML
    private TextField publisherField;
    @FXML
    private TextField pagesField;
    @FXML
    private TextField descriptionField;
    @FXML
    private DatePicker publicationDateField;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    private ObservableList<Book> bookList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        bookNavButton.getStyleClass().add("nav-active");
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorsColumn.setCellValueFactory(new PropertyValueFactory<>("authors"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        pagesColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfPages"));
        publicationDateColumn.setCellValueFactory(new PropertyValueFactory<>("publicationDate"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        bookTable.setItems(bookList);
        loadBooksAsync();
    }

    /*
     * Loads books from the database in background...updates TableView when complete
     */
    public void loadBooksAsync() {
        loadingIndicator.setVisible(true);
        bookTable.setPlaceholder(new Label("")); 
        Task<List<Book>> task = new Task<>() {
            @Override
            protected List<Book> call() {
                BookService bookService = new BookService();
                return bookService.getAllBooks();
            }
        };
        task.setOnSucceeded(e -> {
            bookList.setAll(task.getValue());
            loadingIndicator.setVisible(false);
            if (bookList.isEmpty()) {
                bookTable.setPlaceholder(new Label("No books found."));
            }
        });
        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            bookTable.setPlaceholder(new Label("Failed to load books."));
        });
        new Thread(task).start();
    }

    /*
     * Adds a new book to the database including validation and error handling from
     * user input
     */
    @FXML
    private void handleAdd() {
        try {
            String isbn = isbnField.getText().trim();
            String title = titleField.getText().trim();
            String authors = authorsField.getText().trim();
            String publisher = publisherField.getText().trim();
            String pagesText = pagesField.getText().trim();
            String description = descriptionField.getText().trim();

            // Validate user input
            if (isbn.isEmpty() || title.isEmpty() || authors.isEmpty() || publisher.isEmpty() || pagesText.isEmpty()
                    || publicationDateField.getValue() == null || description.isEmpty()) {
                showAlert("All fields are required.");
                return;
            }
            int numberOfPages;
            try {
                numberOfPages = Integer.parseInt(pagesText);
                if (numberOfPages <= 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showAlert("Number of Pages must be a positive integer.");
                return;
            }
            Book book = new Book();
            book.setIsbn(isbn);
            book.setTitle(title);
            book.setAuthors(authors);
            book.setPublisher(publisher);
            book.setNumberOfPages(numberOfPages);
            book.setPublicationDate(java.sql.Date.valueOf(publicationDateField.getValue()));
            book.setDescription(description);
            BookService bookService = new BookService();
            bookService.createBook(book);
            bookList.add(book);
            isbnField.clear();
            titleField.clear();
            authorsField.clear();
            publisherField.clear();
            pagesField.clear();
            publicationDateField.setValue(null);
            descriptionField.clear();

            // handle errors
        } catch (Exception e) {
            showAlert("Error adding book: " + e.getMessage());
        }
    }

    /*
     * Edits a book in the database including validation and error handling from
     * user input
     */
    @FXML
    private void handleEdit() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a book to edit.");
            return;
        }
        try {
            String isbn = isbnField.getText().trim();
            String title = titleField.getText().trim();
            String authors = authorsField.getText().trim();
            String publisher = publisherField.getText().trim();
            String pagesText = pagesField.getText().trim();
            String description = descriptionField.getText().trim();

            // Validate user input
            if (isbn.isEmpty() || title.isEmpty() || authors.isEmpty() || publisher.isEmpty() || pagesText.isEmpty()
                    || publicationDateField.getValue() == null || description.isEmpty()) {
                showAlert("All fields are required.");
                return;
            }
            int numberOfPages;
            try {
                numberOfPages = Integer.parseInt(pagesText);
                if (numberOfPages <= 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showAlert("Number of Pages must be a positive integer.");
                return;
            }
            selected.setIsbn(isbn);
            selected.setTitle(title);
            selected.setAuthors(authors);
            selected.setPublisher(publisher);
            selected.setNumberOfPages(numberOfPages);
            selected.setPublicationDate(java.sql.Date.valueOf(publicationDateField.getValue()));
            selected.setDescription(description);
            BookService bookService = new BookService();
            bookService.updateBook(selected);
            bookTable.refresh();

            // handle errors
        } catch (Exception e) {
            showAlert("Error editing book: " + e.getMessage());
        }
    }

    /*
     * Deletes a book from the database including error handling
     */
    @FXML
    private void handleDelete() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a book to delete.");
            return;
        }
        try {
            BookService bookService = new BookService();
            boolean deleted = bookService.deleteBook(selected.getBookId());
            if (deleted) {
                bookList.remove(selected);
            } else {
                showAlert("Cannot delete book: It may have borrowed copies.");
            }
        } catch (Exception e) {
            showAlert("Error deleting book: " + e.getMessage());
        }
    }

    /*
     * Navigation Bar Methods
     */
    @FXML
    private void goToHome() throws Exception {
        switchScene("/view/WelcomeView.fxml", "Home");
    }

    @FXML
    private void goToStudent() throws Exception {
        switchScene("/view/StudentView.fxml", "Student Management");
    }

    @FXML
    private void goToLoan() throws Exception {
        switchScene("/view/LoanView.fxml", "Loan Management");
    }

    @FXML
    private void goToBook() throws Exception {
        switchScene("/view/BookView.fxml", "Book Management");
    }

    @FXML
    private void goToBookCopy() throws Exception {
        switchScene("/view/BookCopyView.fxml", "Book Copy Management");
    }

    /*
     * Helper Methods
     */

     // switches to a new scene
    private void switchScene(String fxmlPath, String title) throws Exception {
        Stage stage = (Stage) bookTable.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Library System - " + title);
    }

    // shows an alert with a message
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Book Management");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}