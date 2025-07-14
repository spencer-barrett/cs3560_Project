package com.library.library_system.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.library.library_system.model.Student;
import javafx.scene.control.TextField;
import com.library.library_system.service.StudentService;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;
import javafx.concurrent.Task;
import java.util.List;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

/*
 * Controller for the Student Management View
 * Handles the management of students in the library system
 * Includes CRUD operations for students
 * 
 */

public class StudentViewController {
    /*
     * FXML Elements
     */
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Integer> broncoIdColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> addressColumn;
    @FXML private TableColumn<Student, String> degreeColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private TextField broncoIdField;
    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField degreeField;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button studentNavButton;

    /*
     * Observable List used to store data in the TableView. Updates UI when data is manipulated.
     */
    private ObservableList<Student> cachedStudents = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        studentNavButton.getStyleClass().add("nav-active");
        broncoIdColumn.setCellValueFactory(new PropertyValueFactory<>("broncoId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        degreeColumn.setCellValueFactory(new PropertyValueFactory<>("degree"));

        loadStudentsAsync();
    }

    /*
     * Loads students from the database in background...updates TableView when complete
     */
    public void loadStudentsAsync() {
        loadingIndicator.setVisible(true);
        studentTable.setPlaceholder(new Label("")); // Hide placeholder while loading

        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() {
                StudentService studentService = new StudentService();
                return studentService.getAllStudents();
            }
        };
        task.setOnSucceeded(e -> {
            cachedStudents.setAll(task.getValue());
            studentTable.setItems(cachedStudents);
            loadingIndicator.setVisible(false);
            if (cachedStudents.isEmpty()) {
                studentTable.setPlaceholder(new Label("No students found."));
            }
        });
        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            studentTable.setPlaceholder(new Label("Failed to load students."));
        });
        new Thread(task).start();
    }

    /*
     * Adds a new student to the database including validation and error handling from user input
     */
    @FXML
    private void handleAdd() {
        try {
            int broncoId = Integer.parseInt(broncoIdField.getText().trim());
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String degree = degreeField.getText().trim();

            // Validate user input
            if (name.isEmpty() || address.isEmpty() || degree.isEmpty() || broncoIdField.getText().trim().isEmpty()) {
                showAlert("All fields are required.");
                return;
            }

            Student student = new Student();
            student.setBroncoId(broncoId);
            student.setName(name);
            student.setAddress(address);
            student.setDegree(degree);

            StudentService studentService = new StudentService();
            studentService.addStudent(student);

            cachedStudents.add(student); 

            broncoIdField.clear();
            nameField.clear();
            addressField.clear();
            degreeField.clear();

        // handle error
        } catch (Exception e) {

            System.out.println("Error adding student: " + e.getMessage());
        }
    }

    /*
     * Edits a student in the database including validation and error handling from user input
     */
    @FXML
    private void handleEdit() { // Now labeled 'Update' in the UI
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a student to update.");
            return;
        }
        try {
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String degree = degreeField.getText().trim();

            // Validate user input
            if (name.isEmpty() || address.isEmpty() || degree.isEmpty()) {
                showAlert("All fields are required.");
                return;
            }

            selected.setName(name);
            selected.setAddress(address);
            selected.setDegree(degree);

            StudentService studentService = new StudentService();
            studentService.updateStudent(selected);
            studentTable.refresh();
            showAlert("Student updated successfully.");

        // handle errors
        } catch (Exception e) {
            showAlert("Error updating student: " + e.getMessage());
        }
    }

    /*
     * Deletes a student from the database including error handling
     */
    @FXML
    private void handleDelete() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a student to delete.");
            return;
        }
        try {
            StudentService studentService = new StudentService();
            boolean deleted = studentService.deleteStudent(selected.getBroncoId());
            if (deleted) {
                cachedStudents.remove(selected);
                showAlert("Student deleted successfully.");
            } else {
                showAlert("Cannot delete student: They may have active loans.");
            }

        // handle errors
        } catch (Exception e) {
            showAlert("Error deleting student: " + e.getMessage());
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
        Stage stage = (Stage) studentTable.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Library System - " + title);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Student Management");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 