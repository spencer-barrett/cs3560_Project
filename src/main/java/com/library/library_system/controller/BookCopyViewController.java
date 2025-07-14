package com.library.library_system.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.library.library_system.model.BookCopy;
import com.library.library_system.model.Book;
import com.library.library_system.service.BookCopyService;
import com.library.library_system.service.BookService;
import javafx.concurrent.Task;
import java.util.List;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;


/*
 * Controller for the Book Copy Management View
 * Handles the management of book copies in the library system
 * Includes CRUD operations for book copies
 * 
 */
public class BookCopyViewController {
    /*
     * FXML Elements
     */
    @FXML private Button bookCopyNavButton;
    @FXML private TableView<BookCopy> bookCopyTable;
    @FXML private TableColumn<BookCopy, Integer> copyIdColumn;
    @FXML private TableColumn<BookCopy, String> bookTitleColumn;
    @FXML private TableColumn<BookCopy, String> bookIsbnColumn;
    @FXML private TableColumn<BookCopy, String> statusColumn;
    @FXML private TableColumn<BookCopy, String> barcodeColumn;
    @FXML private TableColumn<BookCopy, String> locationColumn;
    @FXML private TableColumn<BookCopy, String> dueDateColumn;
    @FXML private ComboBox<Book> bookComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private TextField barcodeField;
    @FXML private TextField locationField;

    /*
     * Observable Lists used to store data in the TableViews and ComboBoxes. Updates UI when data is manipulated.
     */
    private ObservableList<BookCopy> bookCopyList = FXCollections.observableArrayList();
    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    private ObservableList<String> statusList = FXCollections.observableArrayList("Available", "Borrowed");

    @FXML
    public void initialize() {
        bookCopyNavButton.getStyleClass().add("nav-active");
        copyIdColumn.setCellValueFactory(new PropertyValueFactory<>("copyId"));
        bookTitleColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getBook().getTitle()));
        bookIsbnColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getBook().getIsbn()));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        
        // due date column logic
        dueDateColumn.setCellValueFactory(cellData -> {
            BookCopy copy = cellData.getValue();
            if (copy.isBorrowed()) {
                BookCopyService bookCopyService = new BookCopyService();
                String result = bookCopyService.fetchAvailabilityAndDueDate(copy.getCopyId());
                if (result.startsWith("Borrowed. Due on: ")) {
                    return new SimpleStringProperty(result.replace("Borrowed. Due on: ", ""));
                } else {
                    return new SimpleStringProperty("");
                }
            } else {
                return new SimpleStringProperty("");
            }
        });
        
        
        bookCopyTable.setItems(bookCopyList);
        statusComboBox.setItems(statusList);
        loadBooksAsync();
        loadBookCopiesAsync();
    }

    /*
     * Loads books from the database in background...updates ComboBox when complete
     */
    private void loadBooksAsync() {
        loadingIndicator.setVisible(true);
        bookCopyTable.setPlaceholder(new Label("")); // Hide placeholder while loading
        Task<List<Book>> task = new Task<>() {
            @Override
            protected List<Book> call() {
                BookService bookService = new BookService();
                return bookService.getAllBooks();
            }
        };
        task.setOnSucceeded(e -> {
            bookList.setAll(task.getValue());
            bookComboBox.setItems(bookList); 
        });
        task.setOnFailed(e -> {
            bookList.clear();
            bookComboBox.setItems(bookList); 
        });
        new Thread(task).start();
    }

    /*
     * Loads book copies from the database in background...updates TableView when complete
     */
    private void loadBookCopiesAsync() {
        loadingIndicator.setVisible(true);
        Task<List<BookCopy>> task = new Task<>() {
            @Override
            protected List<BookCopy> call() {
                BookCopyService bookCopyService = new BookCopyService();
                return bookCopyService.getAllBookCopies();
            }
        };
        task.setOnSucceeded(e -> {
            bookCopyList.setAll(task.getValue());
            loadingIndicator.setVisible(false);
            if (bookCopyList.isEmpty()) {
                bookCopyTable.setPlaceholder(new Label("No book copies found."));
            }
        });
        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            bookCopyTable.setPlaceholder(new Label("Failed to load book copies."));
        });
        new Thread(task).start();
    }

    /*
     * Adds a new book copy to the database including validation and error handling from user input
     */
    @FXML
    private void handleAdd() {
        try {
            Book selectedBook = bookComboBox.getValue();
            String status = statusComboBox.getValue();
            String barcode = barcodeField.getText().trim();
            String location = locationField.getText().trim();

            // Validate user input
            if (selectedBook == null || status == null || status.isEmpty() || barcode.isEmpty() || location.isEmpty()) {
                showAlert("All fields are required.");
                return;
            }
            BookCopy bookCopy = new BookCopy();
            bookCopy.setBook(selectedBook);
            bookCopy.setStatus(status);
            bookCopy.setBarcode(barcode);
            bookCopy.setLocation(location);
            BookCopyService bookCopyService = new BookCopyService();
            bookCopyService.createBookCopy(bookCopy);
            bookCopyList.add(bookCopy);
            bookComboBox.setValue(null);
            statusComboBox.setValue(null);
            barcodeField.clear();
            locationField.clear();

        // handle errors
        } catch (Exception e) {
            showAlert("Error adding book copy: " + e.getMessage());
        }
    }

    /*
     * Edits a book copy in the database including validation and error handling from user input
     */
    @FXML
    private void handleEdit() {
        BookCopy selected = bookCopyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a book copy to edit.");
            return;
        }
        try {
            Book selectedBook = bookComboBox.getValue();
            String status = statusComboBox.getValue();
            String barcode = barcodeField.getText().trim();
            String location = locationField.getText().trim();

            // Validate user input
            if (selectedBook == null || status == null || status.isEmpty() || barcode.isEmpty() || location.isEmpty()) {
                showAlert("All fields are required.");
                return;
            }
            selected.setBook(selectedBook);
            selected.setStatus(status);
            selected.setBarcode(barcode);
            selected.setLocation(location);
            BookCopyService bookCopyService = new BookCopyService();
            bookCopyService.updateBookCopy(selected);
            bookCopyTable.refresh();

        // handle errors
        } catch (Exception e) {
            showAlert("Error editing book copy: " + e.getMessage());
        }
    }

    /*
     * Deletes a book copy from the database including error handling
     */
    @FXML
    private void handleDelete() {
        BookCopy selected = bookCopyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a book copy to delete.");
            return;
        }
        try {
            BookCopyService bookCopyService = new BookCopyService();
            boolean deleted = bookCopyService.deleteBookCopy(selected.getCopyId());
            if (deleted) {
                bookCopyList.remove(selected);
            } else {
                showAlert("Cannot delete book copy: It may be on loan.");
            }

        // handle errors
        } catch (Exception e) {
            showAlert("Error deleting book copy: " + e.getMessage());
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
    
    private void switchScene(String fxmlPath, String title) throws Exception {
        Stage stage = (Stage) bookCopyTable.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Library System - " + title);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Book Copy Management");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
