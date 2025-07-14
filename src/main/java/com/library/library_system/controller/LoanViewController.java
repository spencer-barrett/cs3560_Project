package com.library.library_system.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.library.library_system.model.Loan;
import com.library.library_system.model.Student;
import com.library.library_system.model.BookCopy;
import com.library.library_system.service.StudentService;
import javafx.concurrent.Task;
import java.util.List;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import com.library.library_system.service.BookCopyService;
import com.library.library_system.service.LoanService;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

/*
 * Controller for the Loan Management View
 * Handles the management of loans in the library system
 * Includes CRUD operations for loans
 * 
 */
public class LoanViewController {
    /*
     * FXML Elements
     */
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private DatePicker borrowDatePicker;
    @FXML private DatePicker dueDatePicker;
    @FXML private TableView<BookCopy> bookCopyTable;
    @FXML private TableColumn<BookCopy, String> barcodeColumn;
    @FXML private TableColumn<BookCopy, String> locationColumn;
    @FXML private TableColumn<BookCopy, Boolean> availableColumn;
    @FXML private Button addCopyButton;
    @FXML private Button removeCopyButton;
    @FXML private Button createLoanButton;
    @FXML private Button returnLoanButton;
    @FXML private Button showReceiptButton;
    @FXML private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, Integer> loanIdColumn;
    @FXML private TableColumn<Loan, String> loanStudentColumn;
    @FXML private TableColumn<Loan, String> loanBorrowDateColumn;
    @FXML private TableColumn<Loan, String> loanDueDateColumn;
    @FXML private TableColumn<Loan, String> loanReturnedColumn;
    @FXML private Button loanNavButton;
    @FXML private TableView<BookCopy> selectedBookCopyTable;
    @FXML private TableColumn<BookCopy, String> selectedBarcodeColumn;
    @FXML private TableColumn<BookCopy, String> selectedLocationColumn;
    @FXML private TableColumn<BookCopy, Boolean> selectedAvailableColumn;
    @FXML private TextField barcodeField;
    @FXML private TextField locationField;
    @FXML private Button generateReportButton;

    /*
     * Observable Lists used to store data in the TableViews and ComboBoxes. Updates UI when data is manipulated.
     */
    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private ObservableList<BookCopy> bookCopyList = FXCollections.observableArrayList();
    private ObservableList<Loan> loanList = FXCollections.observableArrayList();
    private ObservableList<BookCopy> selectedBookCopyList = FXCollections.observableArrayList();
    @FXML private ProgressIndicator loadingIndicator; 

    @FXML
    public void initialize() {
        loanNavButton.getStyleClass().add("nav-active");
        studentComboBox.setItems(studentList);
        bookCopyTable.setItems(bookCopyList);
        loanTable.setItems(loanList);
        selectedBookCopyTable.setItems(selectedBookCopyList);

        // cell value factories for bookCopyTable
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        availableColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(!cellData.getValue().isBorrowed()));
        availableColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                setText(empty ? null : (available ? "Available" : "Borrowed"));
            }
        });

        // cell value factories for selectedBookCopyTable
        selectedBarcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        selectedLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        selectedAvailableColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(!cellData.getValue().isBorrowed()));
        selectedAvailableColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                setText(empty ? null : (available ? "Available" : "Borrowed"));
            }
        });

        // cell value factories for loanTable
        loanIdColumn.setCellValueFactory(new PropertyValueFactory<>("loanId"));
        loanStudentColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStudent().getName()));
        loanBorrowDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBorrowDate().toString()));
        loanDueDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDueDate().toString()));
        loanReturnedColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().isReturned() ? "Yes" : "No"));

        loadStudentsAsync();
        loadBookCopiesAsync();
        loadLoansAsync();
    }

    /*
     * Loads students from the database in background...updates ComboBox when complete
     */
    public void loadStudentsAsync() {
        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() {
                StudentService studentService = new StudentService();
                return studentService.getAllStudents();
            }
        };
        task.setOnSucceeded(e -> {
            studentList.setAll(task.getValue());
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
        });
        task.setOnFailed(e -> {
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
            System.out.println("Failed to load students: " + task.getException());
        });
        new Thread(task).start();
    }

    /*
     * Loads book copies from the database in background...updates TableView when complete
     */
    private void loadBookCopiesAsync() {
        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        Task<List<BookCopy>> task = new Task<>() {
            @Override
            protected List<BookCopy> call() {
                BookCopyService bookCopyService = new BookCopyService();
                return bookCopyService.getAllBookCopies();
            }
        };
        task.setOnSucceeded(e -> {
            bookCopyList.setAll(task.getValue());
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
        });
        task.setOnFailed(e -> {
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
            System.out.println("Failed to load book copies: " + task.getException());
        });
        new Thread(task).start();
    }

    /*
     * Loads loans from the database in background...updates TableView when complete
     */
    private void loadLoansAsync() {
        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        Task<List<Loan>> task = new Task<>() {
            @Override
            protected List<Loan> call() {
                LoanService loanService = new LoanService();
                return loanService.getAllLoans();
            }
        };
        task.setOnSucceeded(e -> {
            loanList.setAll(task.getValue());
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
        });
        task.setOnFailed(e -> {
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
            System.out.println("Failed to load loans: " + task.getException());
        });
        new Thread(task).start();
    }

    /*
     * Adds a book copy to the loan
     */
    @FXML
    private void handleAddCopyToLoan() {
        BookCopy selected = bookCopyTable.getSelectionModel().getSelectedItem();
        if (selected != null && !selectedBookCopyList.contains(selected)) {
            selectedBookCopyList.add(selected);
            bookCopyList.remove(selected);
        }
    }

    /*
     * Removes a book copy from the loan
     */
    @FXML
    private void handleRemoveCopyFromLoan() {
        BookCopy selected = selectedBookCopyTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            bookCopyList.add(selected);
            selectedBookCopyList.remove(selected);
        }
    }

    /*
     * Creates a new loan and adds it to the database including validation and error handling from user input
     */
    @FXML
    private void handleCreateLoan() {
        try {
            Student student = studentComboBox.getValue();
            List<BookCopy> selectedCopies = new java.util.ArrayList<>(selectedBookCopyList);

            // Get dates from DatePickers
            java.util.Date borrowDate = borrowDatePicker.getValue() != null ? java.sql.Date.valueOf(borrowDatePicker.getValue()) : null;
            java.util.Date dueDate = dueDatePicker.getValue() != null ? java.sql.Date.valueOf(dueDatePicker.getValue()) : null;

            // Validate user input
            if (student == null || selectedCopies == null || selectedCopies.isEmpty() || borrowDate == null || dueDate == null) {
                showAlert("All fields are required and at least one book copy must be selected.");
                return;
            }
            LoanService loanService = new LoanService();
            loanService.createLoan(student, selectedCopies, borrowDate, dueDate);
            showAlert("Loan created successfully.");
            selectedBookCopyList.clear();
            loadBookCopiesAsync();
            loadLoansAsync();

        // handle errors
        } catch (Exception e) {
            showAlert("Error creating loan: " + e.getMessage());
        }
    }

    /*
     * Returns a loan and updates the database including error handling
     */
    @FXML
    private void handleReturnLoan() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null) {
            showAlert("Select a loan to return.");
            return;
        }
        try {
            LoanService loanService = new LoanService();
            loanService.returnLoan(selectedLoan.getLoanId(), new java.util.Date());
            showAlert("Loan returned successfully.");
            loadBookCopiesAsync();
            loadLoansAsync();

        // handle errors
        } catch (Exception e) {
            showAlert("Error returning loan: " + e.getMessage());
        }
    }

    /*
     * Generates a receipt for a loan and displays it including error handling
     */
    @FXML
    private void handleShowReceipt() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null) {
            showAlert("Select a loan to show receipt.");
            return;
        }
        try {
            LoanService loanService = new LoanService();
            String receipt = loanService.generateReceipt(selectedLoan.getLoanId());
            showAlert(receipt);

        // handle errors
        } catch (Exception e) {
            showAlert("Error generating receipt: " + e.getMessage());
        }
    }

    /*
     * Deletes a loan from the database including error handling
     */
    @FXML
    private void handleDeleteLoan() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null) {
            showAlert("Select a loan to delete.");
            return;
        }
        try {
            LoanService loanService = new LoanService();
            loanService.deleteLoan(selectedLoan.getLoanId());
            showAlert("Loan deleted successfully.");
            loadBookCopiesAsync();
            loadLoansAsync();

        // handle errors
        } catch (Exception e) {
            showAlert("Error deleting loan: " + e.getMessage());
        }
    }

    /*
     * Generates a loan report and displays it in a popup
     */
    @FXML
    private void handleGenerateReport() {
        List<Loan> loans = loanList; 

        // by student name, then by due date
        Map<String, List<Loan>> grouped = loans.stream()
            .collect(Collectors.groupingBy(l -> l.getStudent().getName()));

        StringBuilder report = new StringBuilder();
        grouped.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                report.append("Student: ").append(entry.getKey()).append("\n");
                entry.getValue().stream()
                    .sorted(Comparator.comparing(Loan::getDueDate))
                    .forEach(loan -> report.append("  Loan ID: ").append(loan.getLoanId())
                        .append(", Due: ").append(loan.getDueDate())
                        .append(", Returned: ").append(loan.isReturned() ? "Yes" : "No")
                        .append("\n"));
                report.append("\n");
            });

        // display in a popup
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Loan Report");
        alert.setHeaderText("Loan Report");
        alert.getDialogPane().setPrefWidth(500);
        alert.setContentText(report.toString());
        alert.showAndWait();
    }

    /*
     * Helper Methods
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Loan Management");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        Stage stage = (Stage) loanTable.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Library System - " + title);
    }
}
