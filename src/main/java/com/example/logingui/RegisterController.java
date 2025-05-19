package com.example.logingui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.List;

public class RegisterController implements Initializable {

    @FXML
    private ImageView shieldImageView;
    @FXML
    private Button closeButton;
    @FXML
    private Label registrationMessageLabel;
    @FXML
    private PasswordField setPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label confirmPasswordLabel;
    @FXML
    private TextField firstnameTextField;
    @FXML
    private TextField lastnameTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private Button registerButton;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        Image shieldImage = new Image(getClass().getResource("/com/example/logingui/Images/protection1.png").toExternalForm());
        shieldImageView.setImage(shieldImage);

        // Initialize error message labels as empty
        registrationMessageLabel.setText("");
        confirmPasswordLabel.setText("");
    }

    @FXML
    public void registerButtonOnAction(ActionEvent actionEvent) {
        registerUser();
    }

    @FXML
    public void closeButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();

        // Open the login form if it's not already open
        openLoginForm();
    }

    public void registerUser(){
        // Initialize confirmation message label
        confirmPasswordLabel.setText("");

        // Check if all fields are filled
        if(firstnameTextField.getText().isBlank() || lastnameTextField.getText().isBlank() || 
           usernameTextField.getText().isBlank() || setPasswordField.getText().isBlank()) {
            registrationMessageLabel.setText("Please fill in all fields");
            return;
        }

        // Check if passwords match
        if (setPasswordField.getText().equals(confirmPasswordField.getText())) {
            // Register user in database
            registerUserInDatabase();
        } else {
            confirmPasswordLabel.setText("Password does not match");
            registrationMessageLabel.setText("Registration failed");
        }
    }

    private void registerUserInDatabase() {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        String firstname = firstnameTextField.getText();
        String lastname = lastnameTextField.getText();
        String username = usernameTextField.getText();
        String password = setPasswordField.getText();

        try {
            // First check if username already exists
            String checkUsername = "SELECT * FROM user_account WHERE username = ?";
            PreparedStatement checkStatement = connectDB.prepareStatement(checkUsername);
            checkStatement.setString(1, username);

            if (checkStatement.executeQuery().next()) {
                // Username already exists
                registrationMessageLabel.setText("Username already exists. Please choose another username.");
                checkStatement.close();
                connectDB.close(); // Keep using connectDB.close() for consistency
                return;
            }

            checkStatement.close();

            // Store the password as plain text (no hashing)
            String insertFields = "INSERT INTO user_account(firstname, lastname, username, password) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connectDB.prepareStatement(insertFields);
            statement.setString(1, firstname);
            statement.setString(2, lastname);
            statement.setString(3, username);
            statement.setString(4, password);

            statement.executeUpdate();

            registrationMessageLabel.setText("User has been registered successfully");

            // Clear the fields after successful registration
            clearFields();

            statement.close();
            connectDB.close(); // Keep using connectDB.close() for consistency

            // Automatically close the registration form and return to login after a short delay
            // This gives the user time to see the success message
            new Thread(() -> {
                try {
                    Thread.sleep(1500); // 1.5 second delay
                    Platform.runLater(() -> {
                        // Close the registration form
                        Stage stage = (Stage) registerButton.getScene().getWindow();
                        stage.close();

                        // Open the login form if it's not already open
                        openLoginForm();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            registrationMessageLabel.setText("Database error: " + e.getMessage());
        }
    }

    /**
     * Hashes a password using SHA-256 algorithm
     * @param password The password to hash
     * @return The hashed password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));

            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            // If hashing fails, return the original password (not ideal but prevents registration failure)
            return password;
        }
    }

    private void clearFields() {
        firstnameTextField.clear();
        lastnameTextField.clear();
        usernameTextField.clear();
        setPasswordField.clear();
        confirmPasswordField.clear();
    }

    /**
     * Opens the login form if it's not already open
     */
    private void openLoginForm() {
        try {
            // Check if any login form is already open
            boolean loginFormFound = false;
            List<javafx.stage.Window> windows = javafx.stage.Window.getWindows();
            for (javafx.stage.Window window : windows) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    if (stage.getTitle() != null && stage.getTitle().equals("Login")) {
                        stage.show(); // Show the login form if it was hidden
                        stage.toFront();
                        loginFormFound = true;
                        break;
                    }
                }
            }

            // If no login form is found, create a new one
            if (!loginFormFound) {
                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 520, 400);
                Stage loginStage = new Stage();
                loginStage.initStyle(StageStyle.UNDECORATED);
                loginStage.setTitle("Login");
                loginStage.setScene(scene);
                loginStage.show();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
