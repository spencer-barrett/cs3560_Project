package com.library.library_system.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

public class WelcomeViewController {
    @FXML
    private Button studentButton;
    @FXML
    private Button loanButton;
    @FXML
    private Button bookButton;
    @FXML
    private Button bookCopyButton;

    /*
     * Navigation Bar Methods
     */
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

    private void switchScene(String fxmlPath, String title) throws Exception {
        Stage stage = (Stage) studentButton.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Library System - " + title);
    }
}
